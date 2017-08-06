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
    private volatile boolean done;

    public Relay(String inAddr, int inport, String outAddr, int outport, String outRemoteAddr, int outRemotePort) throws IOException
    {
        rxSocket = inAddr == null ? new DatagramSocket() :
                new DatagramSocket(new InetSocketAddress(inAddr, inport));

        txSocket = outAddr == null ? new DatagramSocket() :
                new DatagramSocket(new InetSocketAddress(outAddr, outport));

        remoteTxAddr = outRemoteAddr == null ? null :
                new InetSocketAddress(outRemoteAddr, outRemotePort);
    }
    /*
        For constructing relay based on an existing relay. The purpose of this is to relay
        in the opposite direction.
     */
    public Relay(Relay mirror, String outRemoteAddr, int outRemotePort)
    {
        rxSocket = mirror.txSocket;
        txSocket = mirror.rxSocket;
        remoteTxAddr = new InetSocketAddress(outRemoteAddr, outRemotePort);
    }
    @Override
    public void run()
    {
        byte buf[] = new byte[1024 * 65];
        DatagramPacket rxPacket = new DatagramPacket(buf, buf.length);

        DatagramPacket txPacket = remoteTxAddr == null ? new DatagramPacket(buf, buf.length) :
                new DatagramPacket(buf, buf.length, remoteTxAddr);

        while (!done)
        {
            try
            {
                rxSocket.receive(rxPacket);
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
