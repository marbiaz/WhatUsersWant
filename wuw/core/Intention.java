

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
class Intention implements Comparable<Intention>, Externalizable {

PeerID remote;
double pasIntent;
double pacIntent;


Intention() {}


Intention(PeerID p) {
  remote = p;
  pasIntent = pacIntent = Double.NaN;
}


Intention(PeerID p, double pas, double pac) {
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
public int compareTo(Intention o) {
  return remote.compareTo(o.remote);
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  remote.writeExternal(out);
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
  remote = new PeerID();
  remote.readExternal(in);
  pasIntent = in.readDouble();
  pacIntent = in.readDouble();
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#toString()
 */
public String toString() {
  return "<Peer: " + remote.toString() + " ;pas= " + pasIntent + " ;pac= " + pacIntent + " >";
}

}
