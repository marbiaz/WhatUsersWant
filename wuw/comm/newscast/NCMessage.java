/**
 * NCMessage.java
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


class NCMessage implements Externalizable {

private NCCacheEntry[] cache;
private boolean reply;


public NCMessage() {}


NCMessage(NCCacheEntry[] cache) {
  this.cache = cache;
  this.reply = false;
}


NCMessage(NCCacheEntry[] cache, boolean isReply) {
  this.cache = cache;
  this.reply = isReply;
}


NCCacheEntry[] getCache() {
  return cache;
}


//void setCache(NCCacheEntry[] cache) {
//  this.cache = cache;
//}
//
//
//void setReply(boolean isReply) {
//  this.reply = isReply;
//}


boolean isReply() {
  return reply;
}


@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  reply = in.readBoolean();
  int degree = in.readInt();
  cache = new NCCacheEntry[degree];
  for (int i = 0; i < degree; i++) {
    cache[i] = new NCCacheEntry();
    cache[i].readExternal(in);
  }
}


@Override
public void writeExternal(ObjectOutput out) throws IOException {
  out.writeBoolean(reply);
  out.writeInt(cache.length);
  for (NCCacheEntry nc : cache) {
    nc.writeExternal(out);
  }
  out.flush();
}
}
