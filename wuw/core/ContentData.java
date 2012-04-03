

package wuw.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;


/**
 * This class collects all the data related to a given content.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 06
 */
public class ContentData implements Comparable<Object>, Externalizable {

public static enum Interest {
  UNKNOWN, NEGLIGIBLE, LOW, HIGH, PRIMARY
};

public static enum Category {
  MOVIES, CARTOONS, NEWS, VIDEOCLIPS, MUSIC, BOOKS
}; // TODO: the CDN should provide this!!


String ID;
int items;
BitSet itemMap; // bitmap for this content
Intention[] intentions; // pas & pac intentions for this content
                        // TODO: order intentions by PeerID
Interest interest; // peer's interest in this content
Category category;
int version;
// PreferenceSet pasPrefs;
// PreferenceSet pacPrefs;


ContentData() {
  intentions = null;
}


//ContentData(String id) {
//  ID = id;
//}


ContentData(String id, int items, Category c, Interest i) {
  ID = id;
  this.items = items;
  itemMap = new BitSet(items);
  interest = i;
  category = c;
  version = 0;
  intentions = new Intention[0];
}


// ContentData(String ID, BitSet items, Interest i, Category c,
// PeerID[] remotePeers, double[][] ints) {
// this.ID = ID;
// itemMap = items;
// interest = i;
// category = c;
// if (remotePeers != null) {
// intentions = new Intention[ints.length];
// for (int j = 0; j < ints.length; j++) {
// intentions[j] = new Intention(remotePeers[j], ints[j][0], ints[j][1]);
// }
// } else {
// intentions = null;
// }
// }


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
  out.writeUTF(interest.toString());
  out.writeUTF(category.toString());
  byte[] b = itemMap.toByteArray();
  out.writeInt(b.length);
  out.write(b);
  out.writeInt(intentions.length);
  for (int i = 0; i < intentions.length; i++) {
    intentions[i].writeExternal(out);
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
  interest = Interest.valueOf(in.readUTF());
  category = Category.valueOf(in.readUTF());
  byte[] b = new byte[in.readInt()];
  in.readFully(b);
  itemMap = BitSet.valueOf(b);
  int i, ints = in.readInt();
  // XXX: record only intentions toward the local peer!!
  PeerID me = Config.getLocalPeer().localID;
  Intention intent = new Intention();
  for (i = 0; i < ints; i++) {
    intent.readExternal(in);
    if (intent.remote.equals(me)) {
      intent.remote = null; //XXX: risky, but useful to avoid memory waste...
      intentions = new Intention[1];
      intentions[0] = intent;
      intent = new Intention();
    }
  }
  if (intentions == null) {
    intentions = new Intention[0];
  }

}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#toString()
 */
public String toString() {
  String res = ID + " (" + items + " items)"
      + " - version " + version + " -- Category : " + category.toString()
      + "\nItemMap : " + itemMap.toString() + "\nInterest : " + interest.toString()
      + "\nIntentions : " + Config.printArray(intentions);
  return res;
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
