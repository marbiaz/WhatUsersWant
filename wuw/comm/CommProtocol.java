

package wuw.comm;

import java.util.ArrayList;

import wuw.comm.TMessage;
import wuw.core.MsgHandler;
import wuw.core.PeerID;


/**
 * This class represents a generic transport protocol, used to send messages
 * through the network.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 19
 */
abstract class CommProtocol implements CommHandler {

/** Local PeerID. */
protected final PeerID pid;
private ArrayList<MsgHandler> msgHandler;
boolean zipData;

private static int sMsgCounter;
private static int rMsgCounter;


CommProtocol(PeerID p) {
  pid = p;
  zipData = true;
  msgHandler = new ArrayList<MsgHandler>();
}


synchronized int incS() {
  ++sMsgCounter;
  return sMsgCounter;
}


synchronized int incR() {
  ++rMsgCounter;
  return rMsgCounter;
}


synchronized MsgHandler getMsgHandler(int index) {
  return msgHandler.get(index);
}


/*
 * (non-Javadoc)
 * @see wuw.comm.CommHandler#dispatch(wuw.comm.TMessage)
 */
public void dispatch(TMessage msg) {
/**/System.out.println("Message # " + incR() + " dispatched.");
  getMsgHandler(msg.getMid()).handleMsg(msg.getPayload());
  return;
}


/*
 * (non-Javadoc)
 * @see wuw.comm.CommHandler#addMsgHandler(wuw.core.MsgHandler)
 */
synchronized public int addMsgHandler(MsgHandler mh) {
  msgHandler.add(mh);
  return msgHandler.size() - 1;
}


/*
 * (non-Javadoc)
 * @see wuw.comm.CommHandler#send(wuw.core.PeerID, int, java.lang.Object)
 */
abstract public void send(PeerID dest, int mid, Object msg);

}
