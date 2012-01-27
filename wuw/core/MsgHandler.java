

package wuw.core;


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
