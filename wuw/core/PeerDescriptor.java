

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
  } else {
    contents = new byte[0];
  }
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

}
