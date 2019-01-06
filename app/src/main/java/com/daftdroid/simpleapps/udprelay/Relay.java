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

package com.daftdroid.simpleapps.udprelay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

class Relay
{
    private static final int MAX_PACKET_SIZE = 64*1024;
    private final Selector selector;
    private final RelaySpec spec;
    private boolean started;
    private volatile boolean stopping;

    private class RelayChannel
    {
        DatagramChannel channel;
        SocketAddress localAddress;
        SocketAddress remoteAddress;
        SelectionKey selectKey;
        final byte buf[] = new byte[MAX_PACKET_SIZE];
        final ByteBuffer buffer = ByteBuffer.wrap(buf);
    }

    private final RelayChannel channelA = new RelayChannel();
    private final RelayChannel channelB = new RelayChannel();

    public Relay(RelaySpec spec, Selector sel) throws IOException {
        channelA.channel = DatagramChannel.open();
        channelA.channel.configureBlocking(false);
        channelB.channel = DatagramChannel.open();
        channelB.channel.configureBlocking(false);

        selector = sel;
        this.spec = spec;

        /*
            Note that as this is UDP, the definition of client and server are somewhat arbitary,
            however, for both the "client" link and the "server" link, at least one end of the
            link needs a well-specified address.
         */

        if (spec.getChanARemoteIP() == null && spec.getChanALocalIP() == null
                || spec.getChanBLocalIP() == null && spec.getChanBRemoteIP() == null) {
            throw new IllegalArgumentException("At least one end of the link must be well known");
        }

    }

    /*
        Must not be called in the UI thread!
     */

    public void initialize() throws IOException
    {
        if (spec.getChanALocalIP() != null) {
            channelA.localAddress = new InetSocketAddress(spec.getChanALocalIP(), spec.getChanALocalPort());
            channelA.channel.socket().bind(channelA.localAddress);
        }
        if (spec.getChanARemoteIP() != null) {
            channelA.remoteAddress = new InetSocketAddress(spec.getChanARemoteIP(), spec.getChanARemotePort());
            channelA.channel.socket().connect(channelA.remoteAddress);
        }
        if (spec.getChanBLocalIP() != null) {
            channelB.localAddress = new InetSocketAddress(spec.getChanBLocalIP(), spec.getChanBLocalPort());
            channelB.channel.socket().bind(channelB.localAddress);
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


    }

    public void select(SelectionKey key) throws IOException
    {
        if (stopping)
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
            throw new IllegalStateException("Unexpected key" + key.toString());
        }
    }

    private void select(RelayChannel selectedChannel, RelayChannel otherChannel) throws IOException
    {
        SelectionKey key = selectedChannel.selectKey;

        int interestOps = key.interestOps();
        int op = key.readyOps() & interestOps;

        if ((op & SelectionKey.OP_READ) != 0)
        {
            ByteBuffer buffer = selectedChannel.buffer;

            // Write the data received to the other channel
            buffer.clear();
            SocketAddress remote = selectedChannel.channel.receive(buffer);

            // Now if we have not yet determined the remote address we get it from here
            if (selectedChannel.remoteAddress == null)
            {
                selectedChannel.remoteAddress = remote;
                selectedChannel.channel.connect(remote);
            }
            // else compare that they match ? TODO

            buffer.limit(buffer.position());
            buffer.rewind();
            otherChannel.channel.write(buffer);

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
        }
    }

    public void startRelay()
    {
        stopping = false;
        // Register ourself with the network thread, this will call initialize for us

        NetworkThread.getNetworkThread().addRelay(this);

        if (started)
            throw new IllegalStateException ("Already started");

        started = true;
    }

    public void stopRelay()
    {
        stopping = true;
        try {channelA.channel.close();} catch (IOException e) {}
        try {channelB.channel.close();} catch (IOException e) {}
    }

}
