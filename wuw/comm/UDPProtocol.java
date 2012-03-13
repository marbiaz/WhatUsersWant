

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
  // starts the receiver thread.
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
    // open a UDP socket to receive msgs.
    DatagramSocket recSocket = new DatagramSocket(pid.getPort());

    // create a new datagram packet for incoming data.
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

/**/   // if (verbosity>3)
        // System.out.println("    UDP : Receiving message from " +
        // msg.getSource().getIP().getHostAddress() + ":" +
        // msg.getSource().getPort() +
        // " to " + pid.getIP().getHostAddress() + ":" + pid.getPort() + ".");

        // notifies the correct protocol of message reception.
        if (pid.equals(msg.getSource()))
          System.err.println("UDP : ERROR : sender == receiver");
        else
          dispatch(msg);
      }
      catch (Exception e) {
/**/   // if (verbosity>3) {
        System.err.println("UDP Receive Exception: " + e.getMessage());
        e.printStackTrace(System.err);
        // }
      }
    }
  }
  catch (SocketException e) {
/**/// if (verbosity>3) {
    System.err.println("UDP Socket Exception: " + e.getMessage());
    e.printStackTrace(System.err);
    // }
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

    // create a UDP datagram with the serialized message.
    DatagramPacket dgm = new DatagramPacket(bytes, bytes.length, dest.getIP(), dest.getPort());
    // create UDP socket.
    DatagramSocket s = new DatagramSocket();
    // send the message to the remote peer.
    s.send(dgm);

/**/// if (verbosity>3) {
    // System.out.println("    UDP : sending message # " + incS() + " from "
    // + pid.getIP().getHostAddress() + ":" + pid.getPort() + " to "
    // + dest.getIP().getHostAddress() + ":" + dest.getPort() + ".");
    // }
    s.close();
  }
  catch (Exception e) {
/**/// if (CommonState.verbosity>3) {
    System.err.println("UDP Send Exception: " + e.getMessage());
    e.printStackTrace();
    // }
  }
}

}
