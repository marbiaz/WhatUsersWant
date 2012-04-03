

package wuw.comm.newscast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import wuw.core.PeerID;


class NCCacheEntry implements Externalizable {

private PeerID peer;
private long ncTimestamp;
private Object ncDescriptor;


NCCacheEntry() {}


NCCacheEntry(PeerID peer, Object descriptor) {
  ncDescriptor = descriptor;
  this.peer = peer;
  ncTimestamp = 0;
}


PeerID getPeerID() {
  return peer;
}


long getTimestamp() {
  return ncTimestamp;
}


void incTimestamp() {
  this.ncTimestamp++;
}


Object getDescriptor() {
  return ncDescriptor;
}


public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  peer = new PeerID();
  peer.readExternal(in);
  ncTimestamp = in.readLong();
  ncDescriptor = in.readObject();
}


public void writeExternal(ObjectOutput out) throws IOException {
  peer.writeExternal(out);
  out.writeLong(ncTimestamp);
  out.writeObject(ncDescriptor);
  out.flush();
}

}
