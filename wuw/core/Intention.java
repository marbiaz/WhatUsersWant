/**
 * Intention.java
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

package wuw.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This class wraps up the information about a peer's intentions towards another
 * peer.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class Intention implements Comparable<Object>, Externalizable {

Neighbor remote;
double pasIntent;
double pacIntent;


Intention() {
  remote = new Neighbor();
}


Intention(Neighbor p) {
  remote = p;
  pasIntent = pacIntent = -1.0;
}


Intention(Neighbor p, double pas, double pac) {
  remote = p;
  pasIntent = pas;
  pacIntent = pac;
}


/*
 * (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  return remote.equals(((Intention)o).remote);
}


/*
 * (non-Javadoc)
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
public int compareTo(Object o) {
  if (o instanceof Intention) {
    return remote.compareTo(((Intention)o).remote);
  }
  return remote.compareTo((PeerID)o);
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  remote.ID.writeExternal(out);
  out.writeDouble(pasIntent);
  out.writeDouble(pacIntent);

  out.flush();
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  // this object will only be retained by the peer if id = Config.getLocalPeer().localID.
  remote.ID.readExternal(in);
  pasIntent = in.readDouble();
  pacIntent = in.readDouble();
}


///*
// * (non-Javadoc)
// * 
// * @see java.lang.Object#toString()
// */
//public String toString() {
//  return "<Peer: " + (remote == null ? "me" : remote.ID.toString())
//      + "; pas = " + pasIntent + "; pac = " + pacIntent + " >";
//}

/*
 * (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
  return "{'pasInt': " + pasIntent + ", 'pacInt': " + pacIntent + "}";
}

}
