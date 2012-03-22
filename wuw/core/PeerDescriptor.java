

package wuw.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This class implements the local peer's descriptor to be send to other peers.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
public class PeerDescriptor implements Comparable<PeerDescriptor>, Externalizable {

PeerID ID;
long timestamp;
ContentData[] contents;


public PeerDescriptor() {}


/*
 * This is meant to be used by the local peer only, to provide its own
 * up-to-date descriptor.
 */
public PeerDescriptor(PeerID p, long tstamp, ContentData[] c) {
  ID = p;
  timestamp = tstamp;
  contents = c;
}


PeerID getPeerID() {
  return ID;
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  ID.writeExternal(out);
  out.writeLong(timestamp);
  int size = contents.length;
  out.writeInt(size);
  if (size > 0) {
    for (ContentData d : contents) {
      synchronized (d) {
        d.writeExternal(out);
      }
    }
  }
  out.flush();
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  ID = new PeerID();
  ID.readExternal(in);
  timestamp = in.readLong();
  int s = in.readInt();
  contents = new ContentData[s];
  for (int i = 0; i < s; i++) {
    contents[i] = new ContentData();
    contents[i].readExternal(in);
  }
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(PeerDescriptor o) { // TODO: what about equal() ??
  return ID.compareTo(o.ID);
}

}
