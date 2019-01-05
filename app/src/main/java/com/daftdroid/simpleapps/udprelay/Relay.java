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
    private final DatagramChannel channelA;
    private final DatagramChannel channelB;
    private SocketAddress chanALocalAddress;   // Our IP on chan A (eg our Wifi connection)
    private SocketAddress chanARemoteAddress;  // The remote IP on chan A (eg the "client" IP)
    private SocketAddress chanBLocalAddress; // Our IP on chan B (eg our GSM connection)
    private SocketAddress chanBRemoteAddress; // The remote IP on chan B (eg a server)
    private final byte buf1[] = new byte[MAX_PACKET_SIZE];
    private final ByteBuffer bufferAtoB = ByteBuffer.wrap(buf1);
    private final byte buf2[] = new byte[MAX_PACKET_SIZE];
    private final ByteBuffer bufferBtoA = ByteBuffer.wrap(buf2);
    private final Selector selector;
    private final RelaySpec spec;
    private boolean started;
    private volatile boolean stopping;

    private SelectionKey selectKeyA, selectKeyB;

    public Relay(RelaySpec spec, Selector sel) throws IOException {
        channelA = DatagramChannel.open();
        channelA.configureBlocking(false);
        channelB = DatagramChannel.open();
        channelB.configureBlocking(false);

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
            chanALocalAddress = new InetSocketAddress(spec.getChanALocalIP(), spec.getChanALocalPort());
            channelA.socket().bind(chanALocalAddress);
        }
        if (spec.getChanARemoteIP() != null) {
            chanARemoteAddress = new InetSocketAddress(spec.getChanARemoteIP(), spec.getChanARemotePort());
            channelA.socket().connect(chanARemoteAddress);
            chanARegisterFor(SelectionKey.OP_READ);
        }
        else
        {
            // Remote address unknown, register for incoming connection
            chanARegisterFor(SelectionKey.OP_READ);
        }
        if (spec.getChanBLocalIP() != null) {
            chanBLocalAddress = new InetSocketAddress(spec.getChanBLocalIP(), spec.getChanBLocalPort());
            channelB.socket().bind(chanBLocalAddress);
        }
        if (spec.getChanBRemoteIP() != null) {
            chanBRemoteAddress = new InetSocketAddress(spec.getChanBRemoteIP(), spec.getChanBRemotePort());
            channelB.socket().connect(chanBRemoteAddress);
            chanBRegisterFor(SelectionKey.OP_READ);
        }
        else
        {
            // Remote address unknown, wait for incoming connection
            chanBRegisterFor(SelectionKey.OP_READ);

        }

    }
    private void chanARegisterFor(int op)
    {
        try {
            selectKeyA = channelA.register(selector, op);
            selectKeyA.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void chanBRegisterFor(int op)
    {
        try {
            selectKeyB = channelB.register(selector, op);
            selectKeyB.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    public void select(SelectionKey key) throws IOException
    {
        int op = key.readyOps() & key.interestOps();

        if (key == selectKeyA)
        {
            if ((op & SelectionKey.OP_READ) != 0)
            {
                // Write the data received from channel A to channel B
                bufferAtoB.clear();
                SocketAddress remote = channelA.receive(bufferAtoB);

                // Now if we have not yet determined the remote address we get it from here
                if (chanARemoteAddress == null)
                {
                    chanARemoteAddress = remote;
                    channelA.connect(remote);
                }
                // else compare that they match ? TODO

                bufferAtoB.limit(bufferAtoB.position());
                bufferAtoB.rewind();
                channelB.write(bufferAtoB);

                // Now wait for that write to complete, before we can reuse this byte buffer
                chanBRegisterFor(SelectionKey.OP_WRITE);
            }

            //  Write operation is NOT mutually exclusive with the above two, it is mutually
            // exclusive with reading from the other channel

            if ((op & SelectionKey.OP_WRITE) != 0)
            {
                // B Write has finished, so A buffer is ready for use again
                chanARegisterFor(SelectionKey.OP_READ);
            }
        }
        else if (key == selectKeyB)
        {
            if ((op & SelectionKey.OP_READ) != 0) // should be mutually exclusive with connect
            {
                // Write the data received from channel B to channel A
                bufferBtoA.clear();
                SocketAddress remote = channelB.receive(bufferBtoA);

                // Now if we have not yet determined the remote address we get it from here
                if (chanBRemoteAddress == null)
                {
                    chanBRemoteAddress = remote;
                    channelB.connect(remote);
                }
                // else compare that they match ? TODO


                bufferBtoA.limit(bufferBtoA.position());
                bufferBtoA.rewind();
                channelA.write(bufferBtoA);

                // Now wait for that write to complete, before we can reuse this byte buffer
                chanARegisterFor(SelectionKey.OP_WRITE);
            }

            //  Write operation is NOT mutually exclusive with the above two, it is mutually
            // exclusive with reading from the other channel

            if ((op & SelectionKey.OP_WRITE) != 0)
            {
                // A Write has finished, so B buffer is ready for use again
                chanBRegisterFor(SelectionKey.OP_READ);
            }
        }
        else
        {
            throw new IllegalStateException("Unexpected key" + key.toString());
        }

        // In all cases, we don't want the same event twice
        key.cancel();
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
        try {channelA.close();} catch (IOException e) {}
        try {channelB.close();} catch (IOException e) {}
    }

}
