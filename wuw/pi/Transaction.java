/**
 * Transaction.java
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

package wuw.pi;

//import java.util.Comparator;

import wuw.core.PeerID;


/**
 * A transaction is any content exchange between the local peer and a remote
 * peer. It can be outgoing (local peer's upload to a remote peer) or incoming
 * (local peer's download from a remote peer). Each transaction identify a
 * single upload or download event that involves a given remote peer and it is
 * about a given item. It can be ongoing or terminated, but it cannot be
 * interrupted. If so, the resumed event is identified in a different
 * Transaction. For instance, an item download from a remote peer that is
 * interrupted once and then resumed (from the very same peer) will be
 * identified by two Transaction objects.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 02
 */
public class Transaction implements Comparable<Transaction>, Cloneable {

public static enum State {
  ON, DONE, WRONG
};


public static enum Type {
  IN, OUT
};

//public static final Comparator<Transaction> altComparator;

//static {
//  altComparator = new Comparator<Transaction>() {
//    public int compare(Transaction t1, Transaction t2) {
//      int res = t1.item - t2.item;
//      if (res == 0) {
//        res = t1.state.ordinal() - t2.state.ordinal();
//      }
//      if (res == 0) {
//        res = t1.remote.compareTo(t2.remote);
//      }
//      return res;
//    }
//  };
//}

private String contentID;
private State state;
private Type type;
private PeerID remote;
private int item;
private long startTimestamp;
private long endTimestamp;
private double actualBandwidth;
private double maxBandwidth;


public Transaction() {}


/**
 * @return the contentID
 */
public String getContentID() {
  return this.contentID;
}


/**
 * @param contentID
 *          the contentID to set
 */
public void setContentID(String contentID) {
  this.contentID = contentID;
}


/**
 * @return the state
 */
public State getState() {
  return this.state;
}


/**
 * @param state
 *          the state to set
 */
public void setState(State state) {
  this.state = state;
}


/**
 * @return the type
 */
public Type getType() {
  return this.type;
}


/**
 * @param type
 *          the type to set
 */
public void setType(Type type) {
  this.type = type;
}


/**
 * @return the remote
 */
public PeerID getRemote() {
  return this.remote;
}


/**
 * @param remote
 *          the remote to set
 */
public void setRemote(PeerID remote) {
  this.remote  = remote;
}


/**
 * @return the item
 */
public int getItem() {
  return this.item;
}


/**
 * @param item
 *          the item to set
 */
public void setItem(int item) {
  this.item = item;
}


/**
 * @return the startTimestamp
 */
public long getStartTimestamp() {
  return this.startTimestamp;
}


/**
 * @param startTimestamp
 *          the startTimestamp to set
 */
public void setStartTimestamp(long startTimestamp) {
  this.startTimestamp = startTimestamp;
}


/**
 * @return the endTimestamp
 */
public long getEndTimestamp() {
  return this.endTimestamp;
}


/**
 * @param endTimestamp
 *          the endTimestamp to set
 */
public void setEndTimestamp(long endTimestamp) {
  this.endTimestamp = endTimestamp;
}


/**
 * @return the actualBandwidth
 */
public double getActualBandwidth() {
  return this.actualBandwidth;
}


/**
 * @param actualBandwidth
 *          the actualBandwidth to set
 */
public void setActualBandwidth(double actualBandwidth) {
  this.actualBandwidth = actualBandwidth;
}


/**
 * @return the maxBandwidth
 */
public double getMaxBandwidth() {
  return this.maxBandwidth;
}


/**
 * @param maxBandwidth
 *          the maxBandwidth to set
 */
public void setMaxBandwidth(double maxBandwidth) {
  this.maxBandwidth = maxBandwidth;
}


///*
// * (non-Javadoc)
// * @see java.lang.Object#toString()
// */ 
//public String toString() {
//  String res = "ContentID : " + contentID + " -- Remote peer : " + remote
//      + "\nState : " + state + " -- Type : " + type + " -- Item : " + item
//      + "\nStarted at : " + startTimestamp
//      + " -- Ended at : " + endTimestamp + "\nBandwidth : " + actualBandwidth
//      + " over " + maxBandwidth + "\n----------";
//  return res;
//}


/*
 * (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
  String res = "('" + contentID + "', '" + remote.toString() + "', '" + state
      + "', '" + type + "', " + item + ")";
  return res;
}

/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(Transaction t) {
  int res = this.remote.compareTo(t.remote);
  if (res == 0) {
    if(t.contentID == null)
      res = -1;
    else
      res = this.contentID.compareTo(t.contentID);
  }
  if (res == 0) {
    res = this.item - t.item;
  }
  if (res == 0) {
    if(t.state == null)
      res = -1;
    else
      res = this.state.ordinal() - t.state.ordinal();
  }
//  if (res == 0) { // FIXME: check if this makes sense...
//    res = (int)(this.endTimestamp - t.endTimestamp);
//  }
  return res;
}


public Transaction clone() {
  try {
    return (Transaction)super.clone();
  }
  catch (CloneNotSupportedException e) {
    return null;
  }
}

}
