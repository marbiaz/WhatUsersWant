/**
 * Neighbor.java
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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import wuw.pi.Transaction;


/**
 * A neighbor is a remote peer that is part of a peer list for at least one of
 * the contents that the local peer trades. Any synchronization on this object
 * must be handled by the caller of its methods, whenever necessary.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 06
 */
class Neighbor implements Comparable<Object> {

PeerID ID; // global!
long timestamp; // TODO: ?? time of latest update to neighbor's data
//for each content, info related to it and this neighbor.
ArrayList<NeighborContentData> contents;
double reputation; // global!


Neighbor() {
  ID = new PeerID();
  reputation = 0;
  timestamp = 0;
}


Neighbor(PeerID p, long tstamp) {
  ID = p;
  timestamp = tstamp;
  reputation = 0;
  contents = new ArrayList<NeighborContentData>();
}


void setPeerID(PeerID p) {
  ID = p;
}


PeerID getPeerID() {
  return ID;
}


String[] getContents() {
  String[] res = new String[contents.size()];
  Iterator<NeighborContentData> it = contents.iterator();
  for (int i = 0; it.hasNext(); i++) {
    res[i] = it.next().getID();
  }
  return res;
}


NeighborContentData getContent(String id) {
  int i = Collections.binarySearch(contents, id);
  if (i >= 0) {
    return contents.get(i);
  }
  return null;
}


NeighborContentData addContent(ContentData c) {
  NeighborContentData nc;
  int i = Collections.binarySearch(contents, c.ID);
  if (i >= 0) {
    nc = contents.get(i);
  } else {
    nc = new NeighborContentData(c);
    contents.add(-i - 1, nc);
  }
  nc.init();
  return nc;
}


void update(ContentData c, Transaction t, long tstamp) {
  NeighborContentData nc = getContent(t.getContentID());
  if (nc == null || nc.downloads == null) {
    nc = addContent(c);
  }
  nc.addTransaction(t);
  timestamp = tstamp;
}


//TODO: add a mechanism to delete some contents the neigh no longer wants to share
// something like : the descr. contains a negative version number whose
// absolute value is higher than the current one, thus the content must be deleted.
// ISSUE: how long must the neigh spread a descriptor with each version number?
boolean update(PeerDescriptor d, long tstamp, String[] conts) {
  NeighborContentData nc = null;
  boolean newer = false; int i;
  ContentData c, cs[] = d.getContents();
  if (cs != null) {
    for (int j = 0; j < cs.length; j++) {
      c = cs[j];
      i = Collections.binarySearch(contents, c.ID);
      if (i >= 0) {
        nc = contents.get(i);
        newer = nc.update(c);
      } else {
        if ((conts == null) || (Arrays.binarySearch(conts, c.ID) >= 0)) {
          nc = new NeighborContentData(c);
          contents.add(-i - 1, nc); // no nc.init, because nc may be not interesting
          newer = true;
        }
      }
    }
    if (newer) {
      timestamp = tstamp;
    }
  }
  return newer;
}


/*
 * (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  if (o instanceof PeerID) {
    return this.ID.equals((PeerID)o);
  }
  return this.ID.equals(((Neighbor)o).ID);
}


/*
 * (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
  String res = ID.toString() + " - " + timestamp + "\nRep. : " + reputation + "\n"
      + ((contents.size() > 0) ? Config.printArray(contents.toArray()) : "");
  return res;
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(Object o) {
  if (o instanceof Neighbor) {
    return this.ID.compareTo(((Neighbor)o).ID);
  }
  return this.ID.compareTo((PeerID)o);
}

}
