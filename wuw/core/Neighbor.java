

package wuw.core;

import java.util.concurrent.ConcurrentHashMap;


/**
 * A neighbor is a remote peer that is part of a peer list for at least one of
 * the contents that the local peer trades. Any synchronization on this object
 * must be handled by the caller of its methods, whenever necessary.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 06
 */
public class Neighbor implements Comparable<Neighbor> {

PeerID ID; // global!
long timestamp; // TODO: ??
ConcurrentHashMap<String, NeighborContentData> contents; // for each content,
                                                         // info related to it
                                                         // and this neighbor.
double reputation; // global!


Neighbor(PeerID p) {
  ID = p;
}


Neighbor(PeerID p, long tstamp) {
  ID = p;
  timestamp = tstamp;
  reputation = 0;
  contents = new ConcurrentHashMap<String, NeighborContentData>();
}


/**
 * @return The ID (see {@link PeerID}) of the neighbor.
 */
public PeerID getPeerID() {
  return ID;
}


/**
 * Merge itemMaps and intentions with a different instance of the same neighbor.
 * After this method has been called, both objects will either contain the union
 * of their data or be unmodified (if they don't have the same ID).
 * 
 * @param n
 *          The neighbor instance to mirror.
 */
public void mirror(Neighbor n) {
  if (!this.equals(n)) return;
  // TODO: who and when will call this method? what to update here? itemMaps for
  // sure. what about intentions?
  timestamp = System.currentTimeMillis();
  n.timestamp = timestamp;
}


public boolean equals(Object o) {
  if (o instanceof PeerID) {
    return this.ID.equals((PeerID)o);
  }
  return this.ID.equals(((Neighbor)o).ID);
}


public String toString() {
  String res = ID.toString() + " - " + timestamp + "\nRep. : " + reputation + "\n"
      + ((contents.size() > 0) ? contents.toString() : "");
  return res;
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(Neighbor n) {
  int res = this.ID.compareTo(n.ID);
  return res;
}

}
