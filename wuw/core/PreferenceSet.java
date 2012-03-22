

package wuw.core;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.ConcurrentHashMap;

import wuw.ui.Preference;


/**
 * This class implements a set of preferences, to be associated to a peer for a
 * content. It is supposed that the number and the names of preferences does not
 * change during the execution, after the initialization, as opposed to their
 * values.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 10
 */
public class PreferenceSet implements Externalizable {

// long contentID;
public ConcurrentHashMap<String, Preference[][]> prefs;
// TODO: Better LinkedHasMap ??


public PreferenceSet() {}


/**
 * It initializes the internal hashmap with the given labels and values. It
 * assumes correct arguments are given.
 * 
 * @param p
 *          Labels of the preferences.
 * @param val
 *          Values of the preferences whose labels are in <code>p</code>, in the
 *          respective order.
 */
PreferenceSet(String[] p, Preference[][][] val) {
  for (int i = 0; i < p.length; i++) {
    prefs.put(p[i], val[i]);
  }
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  // TODO Auto-generated method stub

}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  // TODO Auto-generated method stub

}

}
