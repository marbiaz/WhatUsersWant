

package wuw.core;

import java.io.*;
import java.net.*;


/**
 * This class identifies a peer.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 19
 */
public class PeerID implements Externalizable, Comparable<PeerID> {

/** IP address of the peer */
InetAddress ip;

/** Port number of the peer */
int port;


/**
 * Default (empty) constructor
 */
public PeerID() {};


/**
 * Create a new PeerID from the specified host name and port number.
 */
public PeerID(String host, int port) {
  try {
    this.ip = InetAddress.getByName(host);
  }
  catch (IOException e) {
    System.err.println("Unable to obtain an InetAddress for " + host);
    System.exit(1);
  }
  this.port = port;
}


protected PeerID(InetAddress i, int p) {
  this.ip = i;
  this.port = p;
}


/**
 * @return the peer IP
 */
public InetAddress getIP() {
  return ip;
}


/**
 * @return the peer port
 */
public int getPort() {
  return port;
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#toString()
 */
public String toString() {
  String output = ip.getHostAddress() + ":" + port;
  return output;
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  PeerID id = (PeerID)o;

  return getPort() == id.getPort() && getIP().equals(id.getIP());
}


public int compareTo(PeerID p) {
  return this.toString().compareTo(p.toString());
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  ip = InetAddress.getByName(in.readUTF());
  port = in.readInt();

}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
public void writeExternal(ObjectOutput out) throws IOException {
  out.writeUTF(ip.getHostAddress());
  out.writeInt(port);
  out.flush();
}

}
