package wuw.pi;

import wuw.core.PeerID;
import wuw.pi.BT.BtWuwPeer;


/**
 * This interface defines the services provided by WUW's Program Interface
 * module.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 30
 */
public interface PIHandler {


/**
 * Retrieve information about contents download/upload status. This is the
 * way WUW gets to know the details about the status of the content trading activity. 
 * 
 * @return The latest updates about the content trading on the local peer, or
 *         <code>null</code> if no update is available.
 */
public Transaction[] giveContentUpdates();


/**
 * Get peers from WUW core and make them available to the P2P application, updating its local neighborhood.
 *
 * @param contentID The ID of the content which the peer list must be associated to.
 * @param peers The pere list for the given content.
 */
public void getPeers(String contentID, PeerID[] peers);

/**
 * Get an array of the current best ranked peers according to the  
 * {@code Peer.buildGlobalRanking()} method. This method is called by the tracker 
 * emulator and the list of peers is sent to the local BitTorrent instance
 * @return Array of the best ranked peers
 * @author carvajal-r
 */
public BtWuwPeer[] getBestRankedPeers();

/**
 * Get an array of peers for answering the first announce message which has sent by the 
 * local BitTorrent instance .This method is called by the tracker 
 * emulator and the list of peers is sent to the local BitTorrent instance
 * @return BitTorrent peer list for the first announce message
 * @author carvajal-r
 */
public BtWuwPeer[] getPeersForAnnounce();

/**
 * 
 * @return
 * @author carvajal-r
 */
public String[] getCurrentPeerConnections();

}
