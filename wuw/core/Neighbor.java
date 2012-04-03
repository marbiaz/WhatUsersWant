

package wuw.core;

import java.util.Arrays;
import java.util.LinkedHashMap;

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
LinkedHashMap<String, NeighborContentData> contents; //TODO: concurrent?
double reputation; // global!


Neighbor() {
  ID = new PeerID();
  reputation = 0;
  timestamp = 0;
}


Neighbor(PeerID p) {
  ID = p;
  reputation = 0;
  timestamp = 0;
  contents = new LinkedHashMap<String, NeighborContentData>();
}


Neighbor(PeerID p, long tstamp) {
  ID = p;
  timestamp = tstamp;
  reputation = 0;
  contents = new LinkedHashMap<String, NeighborContentData>();
}


//Neighbor(PeerDescriptor d, long tstamp) {
//  ID = d.ID;
//  timestamp = tstamp;
//  reputation = 0;
//  this.contents = new LinkedHashMap<String, NeighborContentData>();
//  for (ContentData c : d.contents) {
//    this.contents.put(c.ID, new NeighborContentData(c));
//  }
//}


void setPeerID(PeerID p) {
  ID = p;
}


PeerID getPeerID() {
  return ID;
}


//TODO: create contents and transaction lists if necessary!!
//content version = -1 -> intentions etc. null until a descriptor arrives???
void update(Transaction t, long tstamp) {

}


void addContent(ContentData c) {
  NeighborContentData nc;
  if (!contents.containsKey(c.ID)) {
    nc = new NeighborContentData(c);
    contents.put(nc.contentInfo.ID, nc);
  } else {
    nc = contents.get(c.ID);
  }
  nc.init();
}


boolean update(PeerDescriptor d, long tstamp, String[] conts) {
  NeighborContentData lc;
  boolean newer = false;
  ContentData[] cs = d.getContents();
  if (cs != null) {
    for (ContentData c : cs) {
      lc = this.contents.get(c.ID);
      if (lc != null && (c.version > lc.contentInfo.version)) {
        lc.contentInfo = c;
        newer = true;
      } else if (lc == null) {
        if ((conts == null) || (Arrays.binarySearch(conts, c.ID) >= 0)) {
          lc = new NeighborContentData(c);
          this.contents.put(c.ID, lc);
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
      + ((contents.size() > 0) ? Config.printArray(contents.values().toArray()) : "");
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
