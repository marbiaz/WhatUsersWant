

package wuw.pi.BT;

import wuw.pi.Transaction;


public class BitTorrentRequest {

private String ipAddr = null;
private String bitTorrentId = null;
private float currentUplBandwidth;
private float currentDowBandwidth;
private float maxUplBandwidth;
private float maxDowBandwidth;
private Transaction[] transactions = null;


public BitTorrentRequest() {}


public String getIpAddr() {
  return ipAddr;
}


public void setIpAddr(String ipAddr) {
  this.ipAddr = ipAddr;
}


public String getBitTorrentId() {
  return bitTorrentId;
}


public void setBitTorrentId(String bitTorrentId) {
  this.bitTorrentId = bitTorrentId;
}


public float getCurrentUplBandwidth() {
  return currentUplBandwidth;
}


public void setCurrentUplBandwidth(float currentUplBandwidth) {
  this.currentUplBandwidth = currentUplBandwidth;
}


public float getCurrentDowBandwidth() {
  return currentDowBandwidth;
}


public void setCurrentDowBandwidth(float currentDowBandwidth) {
  this.currentDowBandwidth = currentDowBandwidth;
}


public float getMaxUplBandwidth() {
  return maxUplBandwidth;
}


public void setMaxUplBandwidth(float maxUplBandwidth) {
  this.maxUplBandwidth = maxUplBandwidth;
}


public float getMaxDowBandwidth() {
  return maxDowBandwidth;
}


public void setMaxDowBandwidth(float maxDowBandwidth) {
  this.maxDowBandwidth = maxDowBandwidth;
}


public Transaction[] getTransactions() {
  return transactions;
}


public void setTransactions(Transaction[] transactions) {
  this.transactions = transactions;
}


}
