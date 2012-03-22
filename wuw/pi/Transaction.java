

package wuw.pi;

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
public class Transaction implements Comparable<Transaction> {

public static enum State {
  ON, DONE
};


public static enum Type {
  IN, OUT
};

private String contentID;
private State state;
private Type type;
private PeerID remote;
private int item;
private long startTimestamp;
private long endTimestamp;
private double actualBandwidth;
private double maxBandwidth;


Transaction() {}


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
  this.remote = remote;
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


/*
 * (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
  String res = "Remote peer : " + remote.toString() + " -- ContentID : " + contentID + "\nState : "
      + state + " -- Type : " + type + " -- Item : " + item + "\nStarted at : " + startTimestamp
      + " -- Ended at : " + endTimestamp + "\nBandwidth : " + actualBandwidth + " over "
      + maxBandwidth + "\n----------\n";
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
  int c = 0, last = 1000;
  while (res == 0 && c < last) {
    if (c == 0) {
      res = this.contentID.compareTo(t.contentID);
      c = 1;
      continue;
    }
    if (c == 1) {
      res = this.item - t.item;
      c = 2;
      continue;
    }
    if (c == 2) { // FIXME: check if this makes sense...
      res = (int)(this.endTimestamp - t.endTimestamp);
      c = last;
    }
  }
  return res;
}

}
