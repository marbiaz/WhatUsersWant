

package wuw.core;

import java.util.ArrayList;
import java.util.Collections;


/**
 * The neighborhood is the set of all remote peers known by the local peer.
 * Any synchronization on this object must be handled by the caller 
 * of its methods, whenever necessary.
 *
 * @author Marco Biazzini
 * @date 2012 Mar 25
 */
class Neighborhood {

private ArrayList<Neighbor> neighbors;


Neighborhood() {
  neighbors = new ArrayList<Neighbor>();
}


boolean addNeighbor(Neighbor n) {
  int nindex = Collections.binarySearch(neighbors, n);
  boolean res = nindex < 0;
  if (res) {
    neighbors.add(-nindex - 1, n);
  }
  return res;
}


void removeNeighbor(PeerID n) {
  int index = Collections.binarySearch(neighbors, n);
  if (index >= 0) {
    neighbors.remove(index);
    System.err.println("Peer: ATTENTION : removing neighbor " + n.toString());
    System.err.flush();
  }
}


Neighbor getNeighbor(PeerID p) {
  Neighbor n = null;
  int i = Collections.binarySearch(neighbors, p);
  if (i >= 0) {
    n = neighbors.get(i);
  }
  return n;
}


int size() {
  return neighbors.size();
}


Object[] toArray() {
  if (neighbors.size() > 0) {
    return neighbors.toArray();
  }
  return new Object[0];
}

}
