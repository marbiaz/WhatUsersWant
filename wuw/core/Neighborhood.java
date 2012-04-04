

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


void addNeighbor(Neighbor n) {
  int index = Collections.binarySearch(neighbors, n);
  if (index < 0) {
    neighbors.add(-index - 1, n);
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


//Neighbor getNeighbor(int index) {
//  return neighbors.get(index);
//}


Object[] toArray() {
  return neighbors.toArray();
}

}
