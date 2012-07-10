package wuw.pi.BT;

/**
 * Container that for each IP adress has two listening ports (for WUW and  
 * for BitTorrent). Each instance of WUW must have running a BitTorrent process
 * @author carvajal-r
 *
 */
public class BtWuwPeer {

private String ipAddress = null;
private int wuwPort;
private int btPort;

public BtWuwPeer(String ipAddr, int wuwPort, int btPort){
  ipAddress = ipAddr;
  this.wuwPort = wuwPort;
  this.btPort = btPort;
}


public String getIpAddress() {
  return ipAddress;
}


public void setIpAddress(String ipAddress) {
  this.ipAddress = ipAddress;
}


public int getWuwPort() {
  return wuwPort;
}


public void setWuwPort(int wuwPort) {
  this.wuwPort = wuwPort;
}


public int getBtPort() {
  return btPort;
}


public void setBtPort(int btPort) {
  this.btPort = btPort;
}


}
