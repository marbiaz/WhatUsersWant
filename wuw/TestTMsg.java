

package wuw;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import wuw.comm.MsgHandler;
import wuw.core.Config;
import wuw.core.PeerID;


final class TestTMsg implements MsgHandler, Externalizable {

String header;
String footer;
PeerID id;
String more;
static int msgId;


public TestTMsg() {
  id = null;
}


public TestTMsg(String what, PeerID p) {
  header = "Hi, Peer ";
  footer = " speaking! ";
  id = p;
  more = what;
}


void printout() {
  String printline = header + id.toString() + footer + more;
  System.out.println(printline);
  return;
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.core.MsgHandler#handleMsg(java.lang.Object)
 */
@Override
public void handleMsg(Object msg) {
  ((TestTMsg)msg).printout();
  return;
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  out.writeUTF(header);
  id.writeExternal(out);
  out.writeUTF(footer);
  out.writeUTF(more);
  out.flush();
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  header = in.readUTF();
  id.readExternal(in);
  footer = in.readUTF();
  more = in.readUTF();
}


/**
 * A very basic test for the transport protocols.
 * To be called after local peer initialization (see {@link Config#set(String[])}).
 * 
 * @param peerList
 *          An array of PeerIDs of the remote peers to communicate with.
 */
static void transportTest(String peerListFile) {

  PeerID[] peerList = Config.readPeerList(peerListFile, true);
  if (peerList == null) {
    System.exit(2);
  }

  java.util.Random rand = new java.util.Random(System.currentTimeMillis());
  TestTMsg msg = new TestTMsg("Have a nice day!\n", Config.getLocalPeer().getPeerID());
  msgId = Config.getLocalPeer().getTransport().addMsgHandler(msg);
  try {
    for (int i = 0; i < 20; i++) {
      Config.getLocalPeer().getTransport().send(peerList[rand.nextInt(peerList.length)], msgId, msg);
      Thread.sleep(1000);
    }
  }
  catch (InterruptedException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  return;
}

}
