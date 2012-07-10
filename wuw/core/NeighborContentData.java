

package wuw.core;

import java.util.ArrayList;
import java.util.Collections;

import wuw.core.ContentData.Interest;
import wuw.pi.Transaction;
import wuw.pi.Transaction.State;
import wuw.pi.Transaction.Type;

/**
 * Information about a {@link Neighbor} related to a given content.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class NeighborContentData implements Comparable<Object> {


ContentData contentInfo; // for each content, info related to it and this neighbor.
ArrayList<Transaction> downloads; // local peer's incoming transactions with this neighbor
ArrayList<Transaction> uploads; // local peer's outgoing transactions with this neighbor


NeighborContentData(ContentData c) {
  contentInfo = new ContentData(c.ID, c.items, c.category, c.interest);
  downloads = uploads = null;
}


void init() {
  if (downloads == null) {
    downloads = new ArrayList<Transaction>();
    uploads = new ArrayList<Transaction>();
    contentInfo.interest = Interest.UNKNOWN;
    contentInfo.version = -1;
    contentInfo.intentions = new ArrayList<Intention>(1);
    contentInfo.intentions.add(new Intention(null));
  }
}


String getID() {
  return contentInfo.ID;
}


void addTransaction(Transaction t) {
  ArrayList<Transaction> list;
  if (t.getType() == Type.IN) {
    list = downloads;
    contentInfo.itemMap.set(t.getItem());
  } else {
    list = uploads;
    if (t.getState() == State.DONE) {
      contentInfo.itemMap.set(t.getItem());
    }
  }
  int pos = Collections.binarySearch(list, t);//, Transaction.altComparator);
  if (pos < 0) {
    pos = -pos - 1;
  }
  list.add(pos, t);
}


boolean update(ContentData c) {
  if (c.version > contentInfo.version) {
    c.itemMap.or(contentInfo.itemMap);
    contentInfo = c;
    return true;
  }
  return false;
}


public String toString() {
  String res = contentInfo.toString()
      + ((downloads == null || downloads.size() == 0) ? "No downloads\n"
          : "My downloads:\n" + Config.printArray(downloads.toArray()))
      + ((uploads == null || uploads.size() == 0) ? "No uploads\n"
          : "My uploads:\n" + Config.printArray(uploads.toArray()));
  return res;
}


@Override
public int compareTo(Object o) {
  return this.contentInfo.compareTo(o);
}


public boolean equals(Object o) {
  return this.contentInfo.equals(o);
}

}
