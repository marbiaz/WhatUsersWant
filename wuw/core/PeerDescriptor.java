/**
 * PeerDescriptor.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * This class implements the local peer's descriptor to be send to other peers.
 * 
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class PeerDescriptor implements Comparable<PeerDescriptor>, Externalizable {

private PeerID ID;
private static int latestVersion = 0;
private int version;
private byte[] contents;


public PeerDescriptor() {
  contents = new byte[0];
}


/*
 * This is meant to be used by the local peer only, to provide its own
 * up-to-date descriptor.
 */
PeerDescriptor(PeerID p, ContentData[] c) {
  ID = p;
  ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
  GZIPOutputStream gzOut; ObjectOutput oout;
  try {
    gzOut = new GZIPOutputStream(byteOs);
    oout = new ObjectOutputStream(gzOut);
    oout.writeInt(c.length);
    for (ContentData cd : c) {
      cd.writeExternal(oout);
    }
    gzOut.finish();
    oout.flush();
  }
  catch (IOException e) {
    System.err.println("Peer: IO ERROR while creating local peer's descriptor.");
    e.printStackTrace();
    System.err.flush();
  }
  if (byteOs.size() > 0) {
    contents = byteOs.toByteArray();
    version = ++latestVersion;
  } else {
    contents = new byte[0];
  }
}

int getVersion() {
  return version;
}


PeerID getPeerID() {
  return ID;
}


boolean isValid() {
  return contents.length > 0;
}


//TODO: optimization: extract only those contents that are interesting for the local peer
ContentData[] getContents() {
  ContentData[] res;
  try {
    ObjectInput oin = new ObjectInputStream(
        new GZIPInputStream(new ByteArrayInputStream(contents)));
    res = new ContentData[oin.readInt()];
    for (int i = 0; i < res.length; i++) {
      res[i] = new ContentData();
      res[i].readExternal(oin);
    }
  }
  catch (Exception e) {
    System.err.println("Peer: ERROR while inflating remote peer's descriptor (ID = "
        + ID.toString() + ").");
    e.printStackTrace();
    System.err.flush();
    res = null;
  }
  return res;
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  ID.writeExternal(out);
  out.writeInt(version);
  out.writeInt(contents.length);
  if (contents.length > 0) {
    out.write(contents);
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
  ID = new PeerID();
  ID.readExternal(in);
  version = in.readInt();
  contents = new byte[in.readInt()];
  in.readFully(contents);
}


/*
 * (non-Javadoc)
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(PeerDescriptor o) {
  return ID.compareTo(o.ID);
}


/*
 * (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  return ID.equals(((PeerDescriptor)o).ID);
}

public String toString() {
  String res = "()";
  ContentData[] contents = getContents();
  if(contents == null)
    return res;
  else{
    res = "('" + ID.toString() + "', " + version + ", [";
    for(int i = 0; i < contents.length; i++){
      if( i == contents.length - 1 )
        res += contents[i].toString() + "])";
      else
        res += contents[i].toString() + ", ";
    }
  }
  return res;
}

}
