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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

class Relay extends Thread
{
    private final DatagramSocket rxSocket;
    private final DatagramSocket txSocket;
    private final InetSocketAddress remoteTxAddr;
    private volatile InetSocketAddress remoteRxAddr;
    private volatile boolean done;
    private final Relay mirrorOf;

    public Relay(String inAddr, int inport, String outAddr, int outport, String outRemoteAddr, int outRemotePort) throws IOException
    {
        rxSocket = inAddr == null ? new DatagramSocket() :
                new DatagramSocket(new InetSocketAddress(inAddr, inport));

        txSocket = outAddr == null ? new DatagramSocket() :
                new DatagramSocket(new InetSocketAddress(outAddr, outport));

        if (outRemoteAddr == null || outRemotePort <= 0)
        {
            // This is the relay that will target the remote - find the server if you like, the
            // address must be concrete.
            throw new IllegalArgumentException("Remote output address for non-mirror (initiator) may not be ephermeral / unspecified");
        }
        remoteTxAddr = new InetSocketAddress(outRemoteAddr, outRemotePort);

        mirrorOf = null; // This is not a mirror, but another relay may be a mirror of it.
    }
    /*
        For constructing relay based on an existing relay. The purpose of this is to relay
        in the opposite direction.
     */
    public Relay(Relay mirror)
    {
        rxSocket = mirror.txSocket;
        txSocket = mirror.rxSocket;
        remoteTxAddr = null;
        mirrorOf = mirror;
    }
    public Relay(Relay mirror, String outRemoteAddr, int outRemotePort)
    {
        rxSocket = mirror.txSocket;
        txSocket = mirror.rxSocket;
        // As we are a mirror, we CAN pass null for the remote addr, we get it from the relay
        // we are mirroring once it is connected.
        remoteTxAddr = outRemoteAddr == null ? null : new InetSocketAddress(outRemoteAddr, outRemotePort);
        mirrorOf = mirror;
    }


    @Override
    public void run()
    {
        byte buf[] = new byte[1024 * 65];
        InetSocketAddress remoteOutAddr;

        /*
            If we have no output address and we are a mirror, wait relay we are mirrring to connect,
            so we can get it's receive address which is our tx address
         */
        if (remoteTxAddr == null && mirrorOf != null)
        {
            remoteOutAddr = mirrorOf.getRemoteRxAddress(); //waits for connect
        }
        else
        {
            remoteOutAddr = remoteTxAddr;
        }

        DatagramPacket rxPacket = new DatagramPacket(buf, buf.length);

        DatagramPacket txPacket = remoteOutAddr == null ? new DatagramPacket(buf, buf.length) :
                new DatagramPacket(buf, buf.length, remoteOutAddr);

        while (!done)
        {
            try
            {
                rxSocket.receive(rxPacket);

                /*  Now we have received something, figure out the remote's address if we didn't
                    know it. NB as we are the only setter of remoteTxAddr don't need to sync before
                    testing it
                 */
                if (remoteRxAddr == null)
                {
                    synchronized (this)
                    {
                        remoteRxAddr = new InetSocketAddress(rxPacket.getAddress(), rxPacket.getPort());
                        notify();
                    }
                }
                txPacket.setLength(rxPacket.getLength());
                txSocket.send(txPacket);
            }
            catch (IOException e)
            {
                if (!done) {
                    // TODO log error!
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e2) {
                    }
                }
            }
        }
    }

    /*
        Get the remote's address. If unknown WAITS UNTIL CONNECT
     */
    public synchronized InetSocketAddress getRemoteRxAddress()
    {
        while (remoteRxAddr == null)
        {
            try {wait();}
            catch (InterruptedException e) {return null;}
        }
        return remoteRxAddr;
    }

    /*
        Method which causes the thread to quickly run to completion. Closing the sockets
        will cause the thread to wake (with an excpetion) in the event that it is waiting
        on send() or, more likely receive().
     */
    public void terminate()
    {
        done = true;
        rxSocket.close();
        txSocket.close();
    }
}
