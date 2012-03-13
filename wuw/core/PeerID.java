package wuw.core;

import java.io.*;
import java.net.*;
import java.util.Enumeration;


/**
 * This class identifies a peer.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 19
 */
public class PeerID implements Externalizable {

/** IP address of the peer */
InetAddress ip;

/** Port number of the peer */
int port;


/**
 * Default (empty) constructor
 */
public PeerID() {};


/**
 * Create a new PeerID from the local IP address and the specified port number.
 */
public PeerID(int p) {
  this.ip = getCurrentEnvironmentNetworkIp();
  if (this.ip == null) {
    System.err.println("Unable to obtain an IP address");
    System.exit(1);
  }
  this.port = p;
}


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


// FIXME: It does not work on many interfaces... It cannot be used safely.
/**
 * Automatically get the local IP address from the local available network
 * interfaces. The Loopback address is discarded and the private IP is returned
 * only if no public address is available.
 * 
 * @return an InetAddress of the local peer.
 */
private static InetAddress getCurrentEnvironmentNetworkIp() {
  Enumeration<NetworkInterface> netInterfaces = null;
  try {
    netInterfaces = NetworkInterface.getNetworkInterfaces();
  }
  catch (SocketException e) {
    e.printStackTrace();
  }

  while (netInterfaces.hasMoreElements()) {
    NetworkInterface ni = netInterfaces.nextElement();
    Enumeration<InetAddress> address = ni.getInetAddresses();
    while (address.hasMoreElements()) {
      InetAddress addr = address.nextElement();
      if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress()
          && !(addr.getHostAddress().indexOf(":") > -1)) {
        return addr;
      }
    }
  }
  try { // only if no public interface is available
    if (!InetAddress.getLocalHost().isLoopbackAddress()) return InetAddress.getLocalHost();
  }
  catch (UnknownHostException e) {
    e.printStackTrace();
  }
  return null;
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


// public Object clone() {
// NodeID res = new NodeID();
// res.ip = this.ip;
// res.port = this.port;
// return res;
// }

}
