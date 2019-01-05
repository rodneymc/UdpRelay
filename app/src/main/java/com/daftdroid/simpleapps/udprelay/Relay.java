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
    private final DatagramChannel clientChannel;
    private final DatagramChannel serverChannel;
    private SocketAddress chanALocalAddress;   // Our IP on chan A (eg our Wifi connection)
    private SocketAddress chanARemoteAddress;  // The remote IP on chan A (eg the "client" IP)
    private SocketAddress chanBLocalAddress; // Our IP on chan B (eg our GSM connection)
    private SocketAddress chanBRemoteAddress; // The remote IP on chan B (eg a server)
    private final byte buf1[] = new byte[MAX_PACKET_SIZE];
    private final ByteBuffer bufferAtoB = ByteBuffer.wrap(buf1);
    private final byte buf2[] = new byte[MAX_PACKET_SIZE];
    private final ByteBuffer bufferBtoA = ByteBuffer.wrap(buf2);
    private final Selector selector;
    private boolean started;

    private SelectionKey readKeyA, readKeyB, writeKeyA, writeKeyB;

    public Relay(RelaySpec spec, Selector sel) throws IOException {
        clientChannel = DatagramChannel.open();
        clientChannel.configureBlocking(false);
        serverChannel = DatagramChannel.open();
        serverChannel.configureBlocking(false);

        selector = sel;

        /*
            Note that as this is UDP, the definition of client and server are somewhat arbitary,
            however, for both the "client" link and the "server" link, at least one end of the
            link needs a well-specified address.
         */

        if (spec.getClientIP() == null && spec.getChanALocalIP() == null
                || spec.getChanBLocalIP() == null && spec.getServerIP() == null) {
            throw new IllegalArgumentException("At least one end of the link must be well known");
        }

        if (spec.getChanALocalIP() != null) {
            chanALocalAddress = new InetSocketAddress(spec.getChanALocalIP(), spec.getChanALocalPort());
            clientChannel.socket().bind(chanALocalAddress);
        }
        if (spec.getClientIP() != null) {
            chanARemoteAddress = new InetSocketAddress(spec.getClientIP(), spec.getClientPort());
            clientChannel.socket().connect(chanARemoteAddress);
        }
        if (spec.getChanBLocalIP() != null) {
            chanBLocalAddress = new InetSocketAddress(spec.getChanBLocalIP(), spec.getChanBLocalPort());
            serverChannel.socket().bind(chanBLocalAddress);
        }
        if (spec.getServerIP() != null) {
            chanBRemoteAddress = new InetSocketAddress(spec.getServerIP(), spec.getServerPort());
            serverChannel.socket().connect(chanBRemoteAddress);
        }
    }

    private void readRegisterClient()
    {
        try {
            readKeyA = clientChannel.register(selector, SelectionKey.OP_READ);
            readKeyA.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void writeRegisterClient()
    {
        try {
            writeKeyA = clientChannel.register(selector, SelectionKey.OP_WRITE);
            writeKeyA.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void readRegisterServer()
    {
        try {
            readKeyB = serverChannel.register(selector, SelectionKey.OP_READ);
            readKeyB.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void writeRegisterServer()
    {
        try {
            writeKeyB = serverChannel.register(selector, SelectionKey.OP_WRITE);
            writeKeyB.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    public void select(SelectionKey key) throws IOException
    {
        if (key == readKeyA)
        {
            // Write the data received from the client out to the server
            serverChannel.write(bufferAtoB);

            // Now wait for that write to complete, before we can reuse this byte buffer
            writeRegisterServer();
        }
        else if (key == writeKeyB)
        {
            // server Channel write has completed, we can accept another packet from the client
            readRegisterClient();
        }
        else if (key == readKeyB)
        {
            // Write the data received from the server out to the client
            clientChannel.write(bufferBtoA);

            // Now wait for that write to complete, before we can reuse this byte buffer
            writeRegisterClient();
        }
        else if (key == writeKeyA)
        {
            // client channel write has completed, we can accept nother packet from the server
            readRegisterServer();
        }
        else
        {
            throw new IllegalStateException("Unexpected key" + key.toString());
        }

        // In all cases, we don't want the same event twice
        key.cancel();

    }

    public void start()
    {
        if (started)
            throw new IllegalStateException ("Already started");

        started = true;
        readRegisterClient();
        readRegisterServer();
    }

}
