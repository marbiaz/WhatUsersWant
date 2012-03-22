package wuw.comm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import wuw.core.PeerID;


/**
 * This class implements the basic facilities to handle TCP connections with
 * remote peers.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 19
 */
public class TCPProtocol extends CommProtocol {

/**
 * @param p
 *          The ID of the local peer.
 */
public TCPProtocol(PeerID p) {
  super(p);
  Thread acceptConnections = new Thread(new Runnable() {

    public void run() {
      acceptConnectionsThread();
    }
  });
  // acceptConnections.setPriority(Thread.currentThread().getPriority()+1);
  acceptConnections.start();
}


private void acceptConnectionsThread() {
  try {
    ServerSocket recSocket = new ServerSocket(pid.getPort());

    while (true) {
      try {
        final Socket s = recSocket.accept();

        Thread recThread = new Thread(new Runnable() {

          public void run() {
            receiverThread(s);
          }
        });
        // recThread.setPriority(Thread.currentThread().getPriority()+1);
        recThread.start();

      }
      catch (Exception ex) {
        System.err.println("TCP Accept Exception: " + ex.getMessage());
        ex.printStackTrace(System.err);
      }
    }
  }
  catch (IOException ex) {
    System.err.println("TCP IOException: " + ex.getMessage());
    ex.printStackTrace(System.err);
  }
}


private void receiverThread(Socket s) {

  try {
    TMessage msg = new TMessage();
    ObjectInput inStream = null;
    if (zipData) {
      inStream = new ObjectInputStream(new GZIPInputStream(s.getInputStream()));
    } else {
      inStream = new ObjectInputStream(s.getInputStream());
    }
    msg.readExternal(inStream);

/**/ // System.out.println("    TCP : Receiving message from " +
     // msg.getSource().getIP().getHostAddress() + ":" +
     // msg.getSource().getPort() +
     // " to " + pid.getIP().getHostAddress() + ":" + pid.getPort() + ".");
    s.close();

    // forward the received message to the correct level protocol of local node.
    if (pid.equals(msg.getSource()))
      System.err.println("TCP : ERROR : sender == receiver");
    else
      dispatch(msg);

  }
  catch (Exception e) {
    System.err.println("TCP receive Exception: " + e.getMessage());
    e.printStackTrace(System.err);
  }
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.comm.CommProtocol#send(wuw.core.PeerID, java.lang.Object)
 */
public void send(PeerID dest, int mid, Object msg) {
  try {
    TMessage tMsg = new TMessage(pid, mid, msg);

    Socket s = new Socket();
    s.connect(new InetSocketAddress(dest.getIP(), dest.getPort()), 5000);
    if (zipData) {
      GZIPOutputStream gzOut = new GZIPOutputStream(s.getOutputStream());
      ObjectOutput oos = new ObjectOutputStream(gzOut);
      tMsg.writeExternal(oos);
      gzOut.finish();
    } else {
      ObjectOutput oos = new ObjectOutputStream(s.getOutputStream());
      tMsg.writeExternal(oos);
    }

/**/// System.out.println("    TCP: sending message # " + incS() + " from "
    // + pid.getIP().getHostAddress() + ":" + pid.getPort() + " to "
    // + dest.getIP().getHostAddress() + ":" + dest.getPort() + ".");
    s.close();
  }
  catch (Exception e) {
    System.err.println("TCP SendException: " + e.getMessage());
    e.printStackTrace(System.err);
  }
}

}
