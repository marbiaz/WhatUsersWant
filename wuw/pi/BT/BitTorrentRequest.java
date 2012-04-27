

package wuw.pi.BT;

import wuw.pi.Transaction;


class BitTorrentRequest {

private String ipAddr = null;
private String bitTorrentId = null;
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
  return bitTorrentId;
}


void setBitTorrentId(String bitTorrentId) {
  this.bitTorrentId = bitTorrentId;
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
