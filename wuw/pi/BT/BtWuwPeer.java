/**
 * BtWuwPeer.java
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

/**
 * Container that for each IP adress has two listening ports (for WUW and  
 * for BitTorrent). Each instance of WUW must have running a BitTorrent process
 * @author carvajal-r
 *
 */
public class BtWuwPeer {

private String ipAddress = null;
private String preferenceStr = null;
private int wuwPort;
private int btPort;

public BtWuwPeer(String ipAddr, int wuwPort, int btPort, String preferenceStr){
  ipAddress = ipAddr;
  this.wuwPort = wuwPort;
  this.btPort = btPort;
  this.preferenceStr = preferenceStr;
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


public String getPreferenceStr() {
  return preferenceStr;
}

}
