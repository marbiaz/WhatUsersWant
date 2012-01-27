

package wuw.core;

import wuw.comm.CommHandler;


/**
 * An instance of this class will contain all that belongs to the local peer.
 * 
 * @author Marco Biazzini
 * @date 2012 January 20
 */
public class Peer {

final CommHandler transport;
final PeerID localID;


/*
 * Standard constructor. It should not be called but by the {@link Config#set(String[])} method.
 * @param p The {@link PeerID} of the local peer
 * @param t The {@link CommHandler} object associated with the local peer
 */
Peer(PeerID p, CommHandler t) {
  localID = p;
  transport = t;
}


/**
 * @return The ID of the local peer
 */
public PeerID getPeerID() {
  return localID;
}


/**
 * @return The {@link CommHandler} object associated with the local peer
 */
public CommHandler getTransport() {
  return transport;
}

}
