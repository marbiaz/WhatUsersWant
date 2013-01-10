/**
 * ContentData.java
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

package wuw.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;


/**
 * This class collects the data related to a given content.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 06
 */
public class ContentData implements Comparable<Object>, Externalizable {
//FIXME: this class should not be public....


String ID;
int items;
BitSet itemMap; // bitmap for this content
ArrayList<Intention> intentions; // pas & pac intentions for this content
int version;
PreferenceSet preferences;


ContentData() {
  ID = null;
  items = -1;
  itemMap = null;
  intentions = null;
  version = -1;
  preferences = null;
}


ContentData(String id, int items, Map<String, String> preferences) {
  ID = id;
  this.items = items;
  itemMap = new BitSet(items);
  intentions = new ArrayList<Intention>();
  version = 0;
  this.preferences = new PreferenceSet(preferences);
}

ContentData(String id, int items, PreferenceSet preferences) {
  ID = id;
  this.items = items;
  itemMap = new BitSet(items);
  intentions = new ArrayList<Intention>();
  version = 0;
  this.preferences = preferences;
}


String getID() {
  return ID;
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  out.writeInt(version);
  out.writeUTF(ID);
  out.writeInt(items);
  byte[] b = itemMap.toByteArray();
  out.writeInt(b.length);
  out.write(b);
  if (Peer.shareInts) {
    out.writeInt(intentions.size());
    Iterator<Intention> it = intentions.iterator();
    while (it.hasNext()) {
      it.next().writeExternal(out);
    }
  } else {
    out.writeInt(0);
  }
  if(Peer.sharePrefs){
    preferences.writeExternal(out);
  }else{
    out.writeInt(0);
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
  version = in.readInt();
  ID = in.readUTF();
  items = in.readInt();
  byte[] b = new byte[in.readInt()];
  in.readFully(b);
  itemMap = BitSet.valueOf(b);
  int i, ints = in.readInt();
  // XXX: record only intentions and prefs toward the local peer!!
  PeerID me = Config.getLocalPeer().localID;
  intentions = new ArrayList<Intention>(1);
  Intention intent = new Intention();
  for (i = 0; i < ints; i++) {
    intent.readExternal(in);
    if (intent.remote.equals(me)) {
      intent.remote = null; // risky, but useful to avoid memory waste
      intentions.add(intent);
      intent = new Intention();
    }
  }
  if (intentions.size() == 0) {
    intentions.add(new Intention(null));
  }
  preferences = new PreferenceSet();
  preferences.readExternal(in);
}

/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(Object c) {
  if (c instanceof ContentData) {
    return this.ID.compareTo(((ContentData)c).ID);
  }
  return this.ID.compareTo(c.toString());
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  if (o instanceof ContentData) {
    return this.ID.equals(((ContentData)o).ID);
  }
  return this.ID.equals(o.toString());
}

}