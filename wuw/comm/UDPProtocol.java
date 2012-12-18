/**
 * UDPProtocol.java
 * 
 * Copyright 2012
 * This file is part of the WUW (What Users Want) service.
 * 
 * WUW is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or (at your option) any later version.
 * 
 * WUW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Foobar. 
 * If not, see http://www.gnu.org/licenses/.
 */

package wuw.comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import wuw.core.PeerID;


/**
 * This class implements the basic facilities to send and receive UDP datagrams
 * to/from remote peers.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 19
 */
public class UDPProtocol extends CommProtocol {

/**
 * @param p
 *          The port number used by the local peer.
 */
public UDPProtocol(PeerID p) {
  super(p);
  Thread recThread = new Thread(new Runnable() {

    public void run() {
      receiverThread();
    }
  });
  // receiver.setPriority(Thread.currentThread().getPriority()+1);
  recThread.start();
}


private void receiverThread() {

  try {
    DatagramSocket recSocket = new DatagramSocket(pid.getPort());
    byte[] buf = new byte[recSocket.getReceiveBufferSize()];
    DatagramPacket dgm = new DatagramPacket(buf, buf.length);

    while (true) {
      try {
        recSocket.receive(dgm);
        TMessage msg = new TMessage();
        ObjectInput inStream = null;
        if (zipData) {
          inStream = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(dgm.getData())));
        } else {
          inStream = new ObjectInputStream(new ByteArrayInputStream(dgm.getData()));
        }
        msg.readExternal(inStream);

/**/    if (printLogs) System.out.println("    UDP : Receiving message from " +
            msg.getSource().toString() + " to " + pid.getIP().toString() + ".");

        // deliver the payload to the correct handler.
        if (pid.equals(msg.getSource()))
          System.err.println("UDP : ERROR : sender == receiver");
        else
          dispatch(msg);
      }
      catch (Exception e) {
        System.err.println("UDP Receive Exception: " + e.getMessage());
        e.printStackTrace(System.err);
      }
    }
  }
  catch (SocketException e) {
    System.err.println("UDP Socket Exception: " + e.getMessage());
    e.printStackTrace(System.err);
  }
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.comm.CommProtocol#getLocalPeerID()
 */
public PeerID getLocalPeerID() {
  return pid;
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.comm.CommProtocol#send(wuw.core.PeerID, java.lang.Object)
 */
public void send(PeerID dest, int mid, Object msg) {
  try {
    TMessage tMessage = new TMessage(pid, mid, msg);
    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    if (zipData) {
      GZIPOutputStream uMsg = new GZIPOutputStream(byteOs);
      tMessage.writeExternal(new ObjectOutputStream(uMsg));
      uMsg.finish();
    } else {
      ObjectOutput uMsg = new ObjectOutputStream(byteOs);
      tMessage.writeExternal(uMsg);
    }
    byte[] bytes = byteOs.toByteArray();

    DatagramPacket dgm = new DatagramPacket(bytes, bytes.length, dest.getIP(), dest.getPort());
    DatagramSocket s = new DatagramSocket();
    s.send(dgm);

/**/if (printLogs) System.out.println("    UDP : sending message # " + incS() + " from "
        + pid.toString() + " to " + dest.toString() + ".");
    s.close();
  }
  catch (Exception e) {
/**/System.err.println("UDP Send Exception: " + e.getMessage());
    e.printStackTrace();
  }
}

}
