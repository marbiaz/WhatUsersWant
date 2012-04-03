

package wuw.core;

import java.util.Arrays;
import java.util.LinkedList;

import wuw.pi.Transaction;
import wuw.pi.Transaction.State;
import wuw.pi.Transaction.Type;



/**
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class LocalContentData extends ContentData {

LinkedList<Transaction> downloading; // local peer's ongoing incoming transactions
LinkedList<Transaction> uploading; // local peer's ongoing outgoing transactions
LinkedList<Transaction> downloaded; // local peer's terminated incoming transactions
LinkedList<Transaction> uploaded; // local peer's terminated outgoing transactions

/**
 * @param c
 */
LocalContentData(String id, int items, Category c, Interest i) {
  super(id, items, c, i);
}


void initIntentions(Neighbor[] n) {
  super.intentions = new Intention[n.length];
  for (int j = 0; j < n.length; j++) {
    super.intentions[j] = new Intention(n[j]);
  }
  Arrays.sort(super.intentions);
  // FIXME: what about transactions?
}


public String toString() {
  String res = super.toString() ;//+ "\nMy ongoing downloads:\n"
      //+ Config.printArray(downloads.toArray()) + "\nMy ongoing uploads: "
      //+ Config.printArray(uploads.toArray()) + "\nMy terminated downloads:\n"
      //+ Config.printArray(downloads.toArray()) + "\nMy terminated uploads: "
      //+ Config.printArray(uploads.toArray());
  return res;
}

}
