/**
 * ContentBasket.java
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
 * You should have received a copy of the GNU General Public License along with WUW. 
 * If not, see http://www.gnu.org/licenses/.
 */

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
