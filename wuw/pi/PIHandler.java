/**
 * PIHandler.java
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
