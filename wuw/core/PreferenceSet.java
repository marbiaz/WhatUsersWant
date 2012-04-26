

package wuw.core;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


/**
 * This class implements a set of preferences, to be associated to a peer for a
 * content. It is supposed that the number and the names of preferences does not
 * change during the execution, after the initialization, as opposed to their
 * values.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 10
 */
class PreferenceSet implements Externalizable {

LinkedHashMap<String, Object> prefs;


public PreferenceSet() {
  prefs = new LinkedHashMap<String, Object>();
}


PreferenceSet(String[] name, Object[] val) {
  prefs = new LinkedHashMap<String, Object>();
  for (int i = 0; i < name.length; i++) {
    prefs.put(name[i], val[i]);
  }
}


Object getValue(String name) {
  if (prefs.containsKey(name)) {
    return prefs.get(name);
  }
  return null;
}


void setValue(String name, Object value) {
    prefs.put(name, value);
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  if (Peer.sharePrefs) {
    int size = prefs.size();
    out.writeInt(size);
    Entry<String, Object> p;
    Iterator<Entry<String, Object>> set = prefs.entrySet().iterator();
    for (int i = 0; i < size; i++) {
      p = set.next();
      out.writeUTF(p.getKey());
      out.writeObject(p.getValue());
    }
  } else {
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
  int size = in.readInt();
  String name;
  for (int i = 0; i < size; i++) {
    name = in.readUTF();
    prefs.put(name, in.readObject());
  }
}


public String toString() {
  Iterator<Entry<String, Object>> it = prefs.entrySet().iterator();
  Entry<String, Object> e;
  String res = "";
  while (it.hasNext()) {
    e = it.next();
    res += "Label: " + e.getKey() + " -- Value: " + e.getValue().toString() + "\n";
  }
  return res;
}

}
