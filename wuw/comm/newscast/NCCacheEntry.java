/**
 * NCCacheEntry.java
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
