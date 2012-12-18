/**
 * BitTorrentRequest.java
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

package wuw.pi.BT;

import wuw.pi.Transaction;

/**
 * Container of the current pieces interchanges (in form of Transactions objects)  
 * in which a BitTorrent client is involved in.  
 * @author carvajal-r
 */
class BitTorrentRequest {

private String ipAddr = null;
//private String portNumber = null;
private float currentUplBandwidth;
private float currentDowBandwidth;
private float maxUplBandwidth;
private float maxDowBandwidth;
private Transaction[] transactions = null;

BitTorrentRequest() {}


String getIpAddr() {
  return ipAddr;
}


void setIpAddr(String ipAddr) {
  this.ipAddr = ipAddr;
}


//String getBitTorrentId() {
//  return portNumber;
//}


//void setBitTorrentId(String bitTorrentId) {
//  this.portNumber = bitTorrentId;
//}


float getCurrentUplBandwidth() {
  return currentUplBandwidth;
}


void setCurrentUplBandwidth(float currentUplBandwidth) {
  this.currentUplBandwidth = currentUplBandwidth;
}


float getCurrentDowBandwidth() {
  return currentDowBandwidth;
}


void setCurrentDowBandwidth(float currentDowBandwidth) {
  this.currentDowBandwidth = currentDowBandwidth;
}


float getMaxUplBandwidth() {
  return maxUplBandwidth;
}


void setMaxUplBandwidth(float maxUplBandwidth) {
  this.maxUplBandwidth = maxUplBandwidth;
}


float getMaxDowBandwidth() {
  return maxDowBandwidth;
}


void setMaxDowBandwidth(float maxDowBandwidth) {
  this.maxDowBandwidth = maxDowBandwidth;
}


Transaction[] getTransactions() {
  return transactions;
}


void setTransactions(Transaction[] transactions) {
  this.transactions = transactions;
}


}
