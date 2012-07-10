

package wuw.pi.BT;

import wuw.pi.Transaction;

/**
 * Container of the current pieces interchanges (in form of Transactions objects)  
 * in which a BitTorrent client is involved in.  
 * @author carvajal-r
 */
class BitTorrentRequest {

private String ipAddr = null;
private String portNumber = null;
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


String getBitTorrentId() {
  return portNumber;
}


void setBitTorrentId(String bitTorrentId) {
  this.portNumber = bitTorrentId;
}


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
