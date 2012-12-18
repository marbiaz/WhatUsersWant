/**
 * PIStub.java
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

package wuw.pi;

import java.util.Arrays;

import wuw.core.Config;
import wuw.core.PeerID;
import wuw.pi.Transaction.State;
import wuw.pi.Transaction.Type;
import wuw.pi.BT.BtWuwPeer;


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
public PIStub() {
  peers = null;
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
public Transaction[] giveContentUpdates() {
  // Apply a variable item range of [0..9] and
  // a randomly chosen Type at each Transaction
  // and assign it to a peer from the local neighborhood.
  // All transactions lasted 5 secs.
  // Randomly assign a 'DONE' State to some transaction.
  // WARNING : the assignment is NOT consistent w.r.t. the item number!!!
  if (peers == null) return null;
  for (int i = 0; i < trans.length; i++) {
    trans[i].setItem(Config.rand.nextInt(10));
    trans[i].setRemote(peers[Config.rand.nextInt(peers.length)]);
    trans[i].setStartTimestamp(System.currentTimeMillis() - 5000);
    trans[i].setEndTimestamp(System.currentTimeMillis());
    trans[i].setType(Config.rand.nextBoolean() ? Type.IN : Type.OUT);
    trans[i].setState((Config.rand.nextDouble() < 0.3) ? State.DONE :
        (Config.rand.nextDouble() < 0.3) ? State.WRONG : State.ON);
  }
  // return a random number (between 2 and 20) of transactions
  Transaction[] res, r = Arrays.copyOfRange(trans, Config.rand.nextInt(7),
      Config.rand.nextInt(11) + 9);
  res = new Transaction[r.length];
  for (int i =0; i <r.length; i++) {
    res[i] = r[i].clone();
  }
  return res;
}


/* (non-Javadoc)
 * @see wuw.pi.PIHandler#getPeers(wuw.core.PeerID[])
 */
@Override
public void getPeers(String content, PeerID[] peers) {
  this.peers = peers;
}


@Override
public BtWuwPeer[] getBestRankedPeers() {
  // TODO Auto-generated method stub
  return null;
}


@Override
public BtWuwPeer[] getPeersForAnnounce() {
  // TODO Auto-generated method stub
  return null;
}

@Override
public String[] getCurrentPeerConnections(){
  // TODO Auto-generated method stub
  return null;
}

}
