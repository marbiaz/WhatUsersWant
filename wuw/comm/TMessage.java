/**
 * TMessage.java
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

package wuw.comm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import wuw.core.PeerID;


/**
 * Common wrapper for all messages sent and received by WUW instances.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 19
 */
public class TMessage implements Externalizable {

/** The PeerID of the source of the message. */
private PeerID source;

/**
 * ID of the message. It is basically a flag that tells which handler object
 * should be given the payload of the message ( @see
 * wuw.comm.TransportProtocol#dispatch).
 */
private int mid;

/** The payload of the message, to be delivered to the proper handler object. */
private Object payload;


/**
 * Default constructor.
 */
TMessage() {
  source = new PeerID();
}


/**
 * @param id
 *          The {@link #source } of the message
 * @param id
 *          The {@link #mid } of the message
 * @param message
 *          The payload of the message
 */
public TMessage(PeerID src, int id, Object message) {
  source = src;
  mid = id;
  payload = message;
}


/**
 * @return the source
 */
public PeerID getSource() {
  return this.source;
}


/**
 * @param source
 *          the source PeerID to set
 */
public void setSource(PeerID source) {
  this.source = source;
}


/**
 * @return The payload of the message
 */
public Object getPayload() {
  return payload;
}


/**
 * @param message
 *          The payload of this Message
 */
public void setPayload(Object message) {
  this.payload = message;
}


/**
 * @return
 */
public int getMid() {
  return mid;
}


/**
 * @param message
 */
public void setMid(int id) {
  this.mid = id;
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  source.readExternal(in);
  mid = in.readInt();
  payload = in.readObject();
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  source.writeExternal(out);
  out.writeInt(mid);
  out.writeObject(payload);
  out.flush();
}

}
