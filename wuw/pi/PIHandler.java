package wuw.pi;

import wuw.core.PeerID;


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

}
