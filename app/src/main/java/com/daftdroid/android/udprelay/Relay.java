/*
This file is part of UdpRelay.

    Copyright 2017 rodney@daftdroid.com

    UdpRelay is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UdpRelay is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UdpRelay.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.daftdroid.android.udprelay;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

class Relay
{
    private static final int MAX_PACKET_SIZE = 64*1024;
    private static final int MAX_RETRIES = 3;
    private final RelaySpec spec;
    private boolean started;
    private volatile boolean stopping;
    private Throwable error;
    enum ErrorState {NONE, BIND, START_ERROR_UNKNOWN, BUG, READ_ERROR_UNKNOWN,
        WRITE_ERROR_UNKNOWN, DESTINATION_UNREACHABLE};
    private ErrorState errorState = ErrorState.NONE;
    private RelayChannel errorChannel; // The channel that errored.
    private final int uniqueId;

    private static int nextUniqueId;

    public static final int ERROR_SOFT = 1;
    public static final int ERROR_HARD = 2;
    public static final int ERROR_NONE = 0;

    private class RelayChannel
    {
        DatagramChannel channel;
        SocketAddress localAddress;
        SocketAddress remoteAddress;
        SelectionKey selectKey;
        final byte buf[] = new byte[MAX_PACKET_SIZE];
        final ByteBuffer buffer = ByteBuffer.wrap(buf);
        int readRetriesRemaining;
        int writeRetriesRemaining;
    }

    private final RelayChannel channelA = new RelayChannel();
    private final RelayChannel channelB = new RelayChannel();

    public Relay(RelaySpec spec) throws IOException {
        channelA.channel = DatagramChannel.open();
        channelA.channel.configureBlocking(false);
        channelB.channel = DatagramChannel.open();
        channelB.channel.configureBlocking(false);

        this.spec = spec;

        /*
            Note that as this is UDP, the definition of client and server are somewhat arbitary,
            however, for both the "client" link and the "server" link, at least one end of the
            link needs a well-specified address.
         */

        if (spec.getChanARemoteIP() == null && spec.getChanALocalIP() == null
                || spec.getChanBLocalIP() == null && spec.getChanBRemoteIP() == null) {
            throw new IOException("At least one end of the link must specified");
        }
        uniqueId = getNextUniqueId();
    }
    /*
        Constructor for creating a new relay from a suspended one
     */
    public Relay(Relay suspended) throws IOException {
        this(suspended.spec);
        channelA.readRetriesRemaining = suspended.channelA.readRetriesRemaining -1;
        channelA.writeRetriesRemaining = suspended.channelA.writeRetriesRemaining -1;
        channelB.readRetriesRemaining = suspended.channelB.readRetriesRemaining -1;
        channelB.writeRetriesRemaining = suspended.channelB.writeRetriesRemaining -1;

    }

    /*
        Must not be called in the UI thread!
     */

    public void initialize(Selector selector)
    {
        try {
            if (spec.getChanALocalIP() != null) {
                channelA.localAddress = new InetSocketAddress(spec.getChanALocalIP(), spec.getChanALocalPort());
                try {
                    channelA.channel.socket().bind(channelA.localAddress);
                } catch (BindException e) {
                    error = e;
                    errorState = ErrorState.BIND;
                    errorChannel = channelA;
                }
            }
            if (spec.getChanARemoteIP() != null) {
                channelA.remoteAddress = new InetSocketAddress(spec.getChanARemoteIP(), spec.getChanARemotePort());
                channelA.channel.socket().connect(channelA.remoteAddress);
            }
            if (spec.getChanBLocalIP() != null) {
                channelB.localAddress = new InetSocketAddress(spec.getChanBLocalIP(), spec.getChanBLocalPort());
                try {
                    channelB.channel.socket().bind(channelB.localAddress);
                } catch (BindException e) {
                    error = e;
                    errorState = ErrorState.BIND;
                    errorChannel = channelB;
                }
            }
            if (spec.getChanBRemoteIP() != null) {
                channelB.remoteAddress = new InetSocketAddress(spec.getChanBRemoteIP(), spec.getChanBRemotePort());
                channelB.channel.socket().connect(channelB.remoteAddress);
            }

            // Start off with them both ready to receive
            channelA.selectKey = channelA.channel.register(selector, SelectionKey.OP_READ);
            channelB.selectKey = channelB.channel.register(selector, SelectionKey.OP_READ);
            channelA.selectKey.attach(this);
            channelB.selectKey.attach(this);
        } catch (IOException e) {
            /*
                An IOException I wasn't anticipating occured.
             */
            error = e;
            errorState = ErrorState.START_ERROR_UNKNOWN;
        }
    }

    public void select(SelectionKey key)
    {
        if (stopping || errorState != ErrorState.NONE)
            return;
        
        int interestOps = key.interestOps();
        int op = key.readyOps() & interestOps;

        if (key == channelA.selectKey)
        {
            select(channelA, channelB);
        }
        else if (key == channelB.selectKey)
        {
            select(channelB, channelA);
        }
        else
        {
            error = new IllegalStateException("Unexpected key" + key.toString());
            errorState = ErrorState.BUG;
        }
    }

    private void select(RelayChannel selectedChannel, RelayChannel otherChannel)
    {
        SelectionKey key = selectedChannel.selectKey;

        int interestOps = key.interestOps();
        int op = key.readyOps() & interestOps;

        if ((op & SelectionKey.OP_READ) != 0)
        {
            ByteBuffer buffer = selectedChannel.buffer;

            // Write the data received to the other channel
            buffer.clear();

            try {
                SocketAddress remote = selectedChannel.channel.receive(buffer);

                // Received without exception - reset the retry count
                selectedChannel.readRetriesRemaining = MAX_RETRIES;

                // Now if we have not yet determined the remote address we get it from here
                if (selectedChannel.remoteAddress == null) {
                    selectedChannel.remoteAddress = remote;
                    selectedChannel.channel.connect(remote);
                }
                // else compare that they match ? TODO
            } catch (IOException e) {
                // I don't expect an IOException reading from a selected UDP channel, nor on
                // calling connect to a remote that has just sent us message (connect doesn't
                // send any packets with udp anyhow).

                error = e;
                errorState = ErrorState.READ_ERROR_UNKNOWN;
                errorChannel = selectedChannel;
            }
            buffer.limit(buffer.position());
            buffer.rewind();

            try {
                otherChannel.channel.write(buffer);

                // Write without exception - reset retry count
                otherChannel.writeRetriesRemaining = MAX_RETRIES;

            } catch (PortUnreachableException | NoRouteToHostException e) {
                error = e;
                errorState = ErrorState.DESTINATION_UNREACHABLE;
            } catch (IOException e) {
                // TODO is there anything else here we could treat as a soft error?
                error = e;
                errorState = ErrorState.WRITE_ERROR_UNKNOWN;
                errorChannel = otherChannel;
            }

            // Now wait for that write to complete, before we can reuse this byte buffer.
            // Clear read interest for this channel, and set write interest for the other.
            interestOps &= ~ SelectionKey.OP_READ;
            key.interestOps(interestOps);

            SelectionKey otherKey = otherChannel.selectKey;
            otherKey.interestOps(SelectionKey.OP_WRITE | otherKey.interestOps());
        }

        //  Write operation is NOT mutually exclusive with the above two, it is mutually
        // exclusive with reading from the other channel

        if ((op & SelectionKey.OP_WRITE) != 0)
        {
            // After write has finished, the other channel's buffer is ready for use again
            SelectionKey otherKey = otherChannel.selectKey;
            otherKey.interestOps(SelectionKey.OP_READ | otherKey.interestOps());

            // And we are not insterested in writeable again until another one occurs
            interestOps &= ~ SelectionKey.OP_WRITE;
            key.interestOps(interestOps);
        }
    }

    public void startRelay()
    {
        stopping = false;

        if (started)
            throw new IllegalStateException ("Already started");

        started = true;
    }
    public boolean stopping() {
        return stopping;
    }

    public void stopRelay()
    {
        stopping = true;
    }

    public void close()
    {
        if (channelA.selectKey != null)
            channelA.selectKey.cancel();

        if (channelB.selectKey != null)
            channelB.selectKey.cancel();

        try {channelA.channel.close();} catch (IOException e) {}
        try {channelB.channel.close();} catch (IOException e) {}
    }
    public RelaySpec getSpec () {
        return spec;
    }
    public boolean hasError() {
        return errorState != ErrorState.NONE;
    }
    /*
        Returns true if the error is soft, for example port unreachable. Returns false if not,
        (if there is no error, also returns false though the result is not really applicable).
     */
    public boolean softError() {
        return channelA.writeRetriesRemaining > 0 && channelA.readRetriesRemaining > 0
                && channelB.writeRetriesRemaining > 0 && channelB.readRetriesRemaining > 0;
    }
    public int getUniqueId() {
        return uniqueId;
    }
    public static synchronized int getNextUniqueId() {
        return nextUniqueId++;
    }

    // Error message state, readable and writable but not updated by this class
    private String statusMessage;
    public String getStatusMessage() {
        return statusMessage;
    }
    public void setStatusMessage(String msg) {
        statusMessage = msg;
    }

    private int errorLevel;
    public int getErrorLevel() {
        return errorLevel;
    }
    public void setErrorLevel(int level) {
        errorLevel = level;
    }


}
