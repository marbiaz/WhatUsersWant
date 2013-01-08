/**
 * PeerID.java
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
 * You should have received a copy of the GNU General Public License along with WUW. 
 * If not, see http://www.gnu.org/licenses/.
 */

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
