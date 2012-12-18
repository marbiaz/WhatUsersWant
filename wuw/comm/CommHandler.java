/**
 * CommHandler.java
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

package wuw.comm;

import wuw.core.PeerID;


/**
 * An object that handles transport messages must implements this interface.
 *
 * @author Marco Biazzini
 * @date 2012 Jan 27
 */
public interface CommHandler {

/**
 * By calling this method and providing a pointer to a handler, any object can
 * subscribe to a delivery service. This means that the provided handler will be
 * given any incoming message of the returned type.
 *
 * @param mh
 *          The pointer to the receiver object that can handle this message
 * @return The message ID that makes it possible to match messages and handlers.
 */
public int addMsgHandler(MsgHandler mh);


/**
 * Sends message <code>msg</code> to peer <code>dest</code>.
 *
 * @param dest
 *          The destination peer
 * @param mid
 *          The ID of the message, as returned to the caller by
 *          {@link #addMsgHandler}
 * @param msg
 *          message to be sent
 */
public void send(PeerID dest, int mid, Object msg);


/**
 * By calling this method whenever a message is received, the payload will be
 * dispatch to the proper handler ( @see wuw.comm.TMessage).
 *
 * @param msg
 *          The message to be dispatched to the proper handler.
 */
void dispatch(TMessage msg);

}
