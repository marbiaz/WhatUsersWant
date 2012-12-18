/**
 * MsgHandler.java
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


/**
 * Any object that can receive and parse messages from remote objects of the
 * same kind must implement this interface.
 * 
 * @author Marco Biazzini
 * @date 2012 January 20
 */
public interface MsgHandler {

/**
 * It processes the received message. Any care about message synchronization,
 * buffering etc. must be taken by the implementation of this method.
 * 
 * @param msg
 *          The message received by a remote peer-object
 */
public void handleMsg(Object msg);

}
