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
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

class Relay
{
    private final DatagramChannel clientChannel;
    private final DatagramChannel serverChannel;
    private SocketAddress inLocalAddress;   // Our IP for talking to the client
    private SocketAddress inRemoteAddress;  // The client's IP
    private SocketAddress outLocalAddress; // Our IP for talking to the server
    private SocketAddress outRemoteAddress; // The server's IP
    private final byte buf1[] = new byte[512];
    private final ByteBuffer clientToServerBuf = ByteBuffer.wrap(buf1);
    private final byte buf2[] = new byte[512];
    private final ByteBuffer serverToClientBuf = ByteBuffer.wrap(buf2);
    private final Selector selector;
    private boolean started;

    private SelectionKey serverReadKey, clientReadKey, clientWriteKey, serverWriteKey;

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

        if (spec.getClientIP() == null && spec.getAndroidLocalIP() == null
                || spec.getAndroidPublicIP() == null && spec.getServerIP() == null) {
            throw new IllegalArgumentException("At least one end of the link must be well known");
        }

        if (spec.getAndroidLocalIP() != null) {
            inLocalAddress = new InetSocketAddress(spec.getAndroidLocalIP(), spec.getAndroidLocalPort());
            clientChannel.socket().bind(inLocalAddress);
        }
        if (spec.getClientIP() != null) {
            inRemoteAddress = new InetSocketAddress(spec.getClientIP(), spec.getClientPort());
            clientChannel.socket().connect(inRemoteAddress);
        }
        if (spec.getAndroidPublicIP() != null) {
            outLocalAddress = new InetSocketAddress(spec.getAndroidPublicIP(), spec.getAndroidPublicPort());
            serverChannel.socket().bind(outLocalAddress);
        }
        if (spec.getServerIP() != null) {
            outRemoteAddress = new InetSocketAddress(spec.getServerIP(), spec.getServerPort());
            serverChannel.socket().connect(outRemoteAddress);
        }
    }

    private void readRegisterClient()
    {
        try {
            clientReadKey = clientChannel.register(selector, SelectionKey.OP_READ);
            clientReadKey.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void writeRegisterClient()
    {
        try {
            clientWriteKey = clientChannel.register(selector, SelectionKey.OP_WRITE);
            clientWriteKey.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void readRegisterServer()
    {
        try {
            serverReadKey = serverChannel.register(selector, SelectionKey.OP_READ);
            serverReadKey.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    private void writeRegisterServer()
    {
        try {
            serverWriteKey = serverChannel.register(selector, SelectionKey.OP_WRITE);
            serverWriteKey.attach(this);
        }
        catch (ClosedChannelException e)
        {
            throw new IllegalStateException (e);
        }
    }

    public void select(SelectionKey key) throws IOException
    {
        if (key == clientReadKey)
        {
            // Write the data received from the client out to the server
            serverChannel.write(clientToServerBuf);

            // Now wait for that write to complete, before we can reuse this byte buffer
            writeRegisterServer();
        }
        else if (key == serverWriteKey)
        {
            // server Channel write has completed, we can accept another packet from the client
            readRegisterClient();
        }
        else if (key == serverReadKey)
        {
            // Write the data received from the server out to the client
            clientChannel.write(serverToClientBuf);

            // Now wait for that write to complete, before we can reuse this byte buffer
            writeRegisterClient();
        }
        else if (key == clientWriteKey)
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
