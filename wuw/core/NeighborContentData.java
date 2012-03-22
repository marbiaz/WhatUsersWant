

package wuw.core;

import java.util.LinkedList;

import wuw.pi.Transaction;


/**
 * Information about a {@link Neighbor} related to a given content.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class NeighborContentData {


ContentData contentInfo; // for each content, info related to it and this
                         // neighbor.
LinkedList<Transaction> downloads; // my incoming transaction with this neighbor
LinkedList<Transaction> uploads; // my outgoing transactions with this neighbor
double[] myIntentions; // local peer's pas & pac intentions toward this peer
                       // FIXME : duplication!


/**
 * @param c
 */
NeighborContentData(ContentData c) {
  contentInfo = c;
  downloads = new LinkedList<Transaction>();
  uploads = new LinkedList<Transaction>();
  // myIntentions =
}


String getID() {
  return contentInfo.ID;
}

}
