

package wuw.pi;

import java.util.Arrays;

import wuw.core.Config;
import wuw.core.PeerID;
import wuw.pi.Transaction.State;
import wuw.pi.Transaction.Type;


/**
 * A stub class to mimic a program interface and feed the local peer with some
 * emulate content.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 02
 */
public class PIStub implements PIHandler {


Transaction[] trans;
PeerID[] peers;


/*
 * Creates fake transactions for 20 contents. The bandwidth is fixed.
 */
public PIStub(PeerID[] p) {
  peers = p;
  trans = new Transaction[20];
  for (int i = 0; i < trans.length; i++) {
    trans[i] = new Transaction();
    trans[i].setContentID("Content" + (i + 1));
    trans[i].setMaxBandwidth(100);
    trans[i].setActualBandwidth(50);
  }
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.pi.PIHandler#getContentUpdates(java.lang.String)
 */
@Override
public Transaction[] getContentUpdates() {
  // Apply a variable item range of [0..9] and
  // a randomly chosen Type at each Transaction
  // and assign it to a peer from the local neighborhood.
  // All transactions lasted 5 secs.
  // Randomly assign a 'DONE' State to some transaction.
  // WARNING : the assignment is NOT consistent w.r.t. the item number!!!
  for (int i = 0; i < trans.length; i++) {
    trans[i].setItem(Config.rand.nextInt(10));
    trans[i].setRemote(peers[Config.rand.nextInt(peers.length)]);
    trans[i].setStartTimestamp(System.currentTimeMillis() - 5000);
    trans[i].setEndTimestamp(System.currentTimeMillis());
    trans[i].setType(Config.rand.nextBoolean() ? Type.IN : Type.OUT);
    trans[i].setState((Config.rand.nextDouble() < 0.3) ? State.DONE : State.ON);
  }
  // return a random number (between 2 and 20) of transactions
  return Arrays.copyOfRange(trans, Config.rand.nextInt(7), Config.rand.nextInt(11) + 9);
}

}
