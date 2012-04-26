

package wuw.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/*
 * This class encapsulates the internal representation of the set of contents
 * on the local peer and makes its managing transparent.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 25
 */
class ContentBasket {

private ArrayList<LocalContentData> contents;


ContentBasket() {
  contents = new ArrayList<LocalContentData>();
}


LocalContentData getContent(String c) {
  LocalContentData res = null;
  int i = Collections.binarySearch(contents, c);
  if (i >= 0) {
    res = contents.get(i);
  }
  return res;
}


int addContent(LocalContentData n) {
  Config.addUnique(contents, n);
  return contents.size();
}


void addNeighbor(Neighbor n) {
  int c;
  LocalContentData cd;
  NeighborContentData nc;
  // XXX: it should better be: for (String s : n.getContents()) {nc = n.getContent(s)}
  for (c = 0; c < n.contents.size(); c++) {
    nc = n.contents.get(c);
    cd = getContent(nc.getID());
    if (cd != null) {
      cd.addNeighbor(n);
      if (nc.downloads == null) {
        nc.init();
      }
    }
  }
}


int size() {
  return contents.size();
}

String[] getIDs() {
  if (contents.size() == 0) return null;
  String[] res = new String[contents.size()];
  Iterator<LocalContentData> it = contents.iterator();
  for (int i = 0; it.hasNext(); i++) {
    res[i] = it.next().ID;
  }
  return res;
}


ContentData[] toArray() {
  LocalContentData[] a = new LocalContentData[contents.size()];
  return contents.toArray(a);
}

}
