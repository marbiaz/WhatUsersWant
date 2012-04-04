

package wuw.core;

import java.util.LinkedList;

import wuw.core.ContentData.Interest;
import wuw.pi.Transaction;


/**
 * Information about a {@link Neighbor} related to a given content.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class NeighborContentData {


ContentData contentInfo; // for each content, info related to it and this neighbor.
LinkedList<Transaction> downloads; // local peer's incoming transactions with this neighbor
LinkedList<Transaction> uploads; // local peer's outgoing transactions with this neighbor


NeighborContentData(ContentData c) {
  contentInfo = new ContentData(c.ID, c.items, c.category, c.interest);
  downloads = uploads = null;
}


void init() {
  if (downloads == null) {
    downloads = new LinkedList<Transaction>();
    uploads = new LinkedList<Transaction>();
    contentInfo.interest = Interest.UNKNOWN;
    contentInfo.version = -1;
    contentInfo.intentions = new Intention[1];
    contentInfo.intentions[0] = new Intention(null);
  }
}


String getID() {
  return contentInfo.ID;
}


public String toString() {
  String res = contentInfo.toString() + "\nMy downloads:\n" + Config.printArray(downloads.toArray())
      + "\nMy uploads:\n" + Config.printArray(uploads.toArray());
  return res;
}

}
