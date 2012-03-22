

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
public class ContentData implements Comparable<ContentData>, Externalizable {

public static enum Interest {
  NEGLIGIBLE, LOW, HIGH, PRIMARY
};

public static enum Category {
  MOVIES, CARTOONS, NEWS, VIDEOCLIPS, MUSIC, BOOKS
}; // TODO: the CDN should provide this!!


String ID;
BitSet itemMap; // neighbor's bitmap for this content
Intention[] intentions; // pas & pac intentions toward other peers for this
                        // content TODO: ordered by PeerID
Interest interest; // peer's interest in this content TODO: is it better to
                   // automatically infer it for neighbors?
Category category;
int version;


// PreferenceSet pasPrefs;
// PreferenceSet pacPrefs;


ContentData() {}


ContentData(String id) {
  ID = id;
}


public ContentData(String id, int items, Category c, Interest i, PeerID[] n) {
  ID = id;
  itemMap = new BitSet(items);
  interest = i;
  category = c;
  version = 0;
  intentions = new Intention[n.length];
  for (int j = 0; j < intentions.length; j++) {
    intentions[j] = new Intention(n[j]);
  }
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
  out.writeUTF(interest.toString());
  out.writeUTF(category.toString());
  byte[] b = itemMap.toByteArray();
  out.writeInt(b.length);
  out.write(b);
  out.writeInt(intentions.length); // TODO: optimization -> no need to send NaN values
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
  interest = Interest.valueOf(in.readUTF());
  category = Category.valueOf(in.readUTF());
  byte[] b = new byte[in.readInt()];
  in.readFully(b);
  itemMap = BitSet.valueOf(b);
  intentions = new Intention[in.readInt()];
  for (int i = 0; i < intentions.length; i++) {
    intentions[i] = new Intention();
    intentions[i].readExternal(in);
  }

}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#toString()
 */
public String toString() {
  String res = "\nID : " + ID + " - version " + version + " -- Category : " + category.toString()
      + "\nItemMap : " + itemMap.toString() + "\nInterest : " + interest.toString()
      + "\nIntentions : " + Config.printArray(intentions) + "\n";
  return res;
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(ContentData c) {
  return this.ID.compareTo(c.ID);
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  return this.ID.equals(((ContentData)o).ID);
}

}
