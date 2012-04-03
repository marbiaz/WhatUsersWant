

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


void addContent(LocalContentData n) {
  int index = Collections.binarySearch(contents, n);
  if (index < 0) {
    contents.add(-index - 1, n);
  }
}


int size() {
  return contents.size();
}

String[] getIDs() {
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
