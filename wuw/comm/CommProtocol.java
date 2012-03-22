

package wuw.comm;

import java.util.ArrayList;

import wuw.comm.TMessage;
import wuw.core.Config;
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

boolean printLogs;

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
  printLogs = Config.printLogs;
}


synchronized int incS() {
  ++sMsgCounter;
  return sMsgCounter;
}


synchronized int incR() {
  ++rMsgCounter;
  return rMsgCounter;
}


MsgHandler getMsgHandler(int index) {
  MsgHandler mh;
  synchronized (msgHandler) {
    mh = msgHandler.get(index);
  }
  return mh;
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.comm.CommHandler#dispatch(wuw.comm.TMessage)
 */
public void dispatch(TMessage msg) {
/**/if (printLogs) System.out.println("Message # " + incR() + " dispatched.");
  if (msg.getMid() < msgHandler.size()) {
    getMsgHandler(msg.getMid()).handleMsg(msg.getPayload());
  } else {
/**/System.err.println("Comm : received a message for an inexistant handler (#"
        + msg.getMid() + ")");
  }
  return;
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.comm.CommHandler#addMsgHandler(wuw.core.MsgHandler)
 */
public int addMsgHandler(MsgHandler mh) {
  int i;
  synchronized (msgHandler) {
    msgHandler.add(mh);
    i = msgHandler.size() - 1;
  }
  return i;
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.comm.CommHandler#send(wuw.core.PeerID, int, java.lang.Object)
 */
abstract public void send(PeerID dest, int mid, Object msg);

}
