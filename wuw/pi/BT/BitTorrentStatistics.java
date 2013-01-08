/**
 * BitTorrentStatistics.java
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

package wuw.pi.BT;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import wuw.core.PeerID;
import wuw.pi.Transaction;


/**
 * The BitTorrentStatistics class lets to decode a Python dictionary (data
 * structure that contains BitTorrent statistics for WUW) into Java data types, 
 * for each attribute in this class
 * 
 * @author carvajal-r
 */
class BitTorrentStatistics {

/*
 * These attributes are useful for decode process
 */
@SuppressWarnings("serial")
private HashMap<String, String> expectedValues = new HashMap<String, String>() {
  {
    put("s", "s");
    put("cid", "cid");
    put("dp", "dp");
    put("dps", "dps");
    put("cn", "cn");
    put("cns", "cns");
  }
};

/*
 * These attributes are measures of the BitTorrent performance
 */
private float seconds;
private String contentId = null;
private int downPiecesSize;
private int connectionsSize;
private String downPiecesValuesStr = null;
private String connectionsStr = null;
private PieceDownTime[] dowPiecesTimes = null;
private String[] connectionIps = null;
private HashMap<String, BitTorrentRequest> bTstatistics = 
new HashMap<String, BitTorrentRequest>();
private HashMap<String, Integer> wuwPorts;


/**
 * Once this constructor is called, the Python dictionary will be decode.
 * 
 * @param source String representation of the Python dictionary
 */
BitTorrentStatistics(String source, HashMap<String, Integer> wuwPorts) {
  /*
   * Scanner class with the specified delimiter lets to get an easy handle way
   * for keys and values in the Python dictionary.
   */
  this.wuwPorts = wuwPorts;
  Scanner decoder = new Scanner(source).useDelimiter("\\{\\}|\\,\\s\\'\\_|\\'\\:\\s|\\{\\'\\_|\\}");
  String key, value;
  while (decoder.hasNext()) {
    key = decoder.next();
    value = decoder.next();
    if (value != null) {
      if (key.equals(expectedValues.get(key))) {
        if (key.equals("s")) {
          seconds = Float.valueOf(value);
        }
        if (key.equals("cid")) {
          // Remove character "'" (Python Str type)  
          contentId = value.substring(1, value.length() - 1);
        }
        if (key.equals("dps")) {
          downPiecesSize = Integer.valueOf(value);
        }
        if (key.equals("dp")) {
          downPiecesValuesStr = value;
        }
        if( key.equals("cns") ){
          connectionsSize = Integer.valueOf(value);
        }
        if( key.equals("cn") ){
          connectionsStr = value;
        }
      } else {
        if (hasExpectFormat(key)) {
          decodeBitTorrentReqs(key, value);
        } else {
          System.out.println("Key has not the expected format");
          System.exit(1);
        }
      }
    } else {
      System.out.println("The dictionary received of BitTorrent is not well formed");
      System.exit(1);
    }
  }
  decoder.close();
  dowPiecesTimes = decodeDowPiecesTimes();
  connectionIps = decodeCurrentConnections();
}

/**
 * Determines if the received Python key has the expected format
 * @param key String representation of the Python key
 * @return {@code True} if the format is corrent, otherwise it returns {@code False} 
 */
boolean hasExpectFormat(String key) {
  String[] valuesInKey = key.split("\\_");
  if (valuesInKey.length != 6) {
    System.out.println("The number of values in key is incorrect");
    return false;
  }
  String ipAddress = valuesInKey[0];
  if (!ipAddress.matches("((\\d)*\\.)*(\\d)*")) {
    System.out.println("Ip address has not a rigth format");
    return false;
  }
  String regularExprForFloat = "(\\d)*\\.(\\d)*|(\\d)*";
  String currUplBan = valuesInKey[1];
  if (!currUplBan.matches(regularExprForFloat)) {
    System.out.println("Expected float value (currUplB) is not right");
    return false;
  }
  String currDowBand = valuesInKey[2];
  if (!currDowBand.matches(regularExprForFloat)) {
    System.out.println("Expected float value (currDowB) is not right");
    return false;
  }
  String maxUplBand = valuesInKey[3];
  if (!maxUplBand.matches(regularExprForFloat)) {
    System.out.println("Expected float value (maxUplB) is not right");
    return false;
  }
  String maxDowBand = valuesInKey[4];
  if (!maxDowBand.matches(regularExprForFloat)) {
    System.out.println("Expected float value (dowUplB) is not right");
    return false;
  }
  String transactions = valuesInKey[5];
  if (!transactions.matches(regularExprForFloat)) {
    System.out.println("Expected float value (transactions) is not right");
    return false;
  }
  return true;
}

/**
 * Main use case for decoding the received Python's key-object pair into a Java's 
 * key-object pair
 * @param key String representation of Python key
 * @param value String representation of a Python object
 */
void decodeBitTorrentReqs(String key, String value) {
  String[] valuesInKey = key.split("\\_");
  String ipAddress = valuesInKey[0];
  float currUplBan = Float.valueOf(valuesInKey[1]);
  float currDowBand = Float.valueOf(valuesInKey[2]);
  float maxUplBand = Float.valueOf(valuesInKey[3]);
  float maxDowBand = Float.valueOf(valuesInKey[4]);
  int transactions = Integer.valueOf(valuesInKey[5]);
  Transaction[] transactionArr = decodeTransactions(value, 
      transactions, ipAddress);
  BitTorrentRequest bTreqsInfo = new BitTorrentRequest();
  bTreqsInfo.setIpAddr(ipAddress);
  // bTreqsInfo.setBitTorrentId(bitTorrentId);
  bTreqsInfo.setCurrentUplBandwidth(currUplBan);
  bTreqsInfo.setCurrentDowBandwidth(currDowBand);
  bTreqsInfo.setMaxUplBandwidth(maxUplBand);
  bTreqsInfo.setMaxDowBandwidth(maxDowBand);
  bTreqsInfo.setTransactions(transactionArr);
  String peerId = ipAddress;
  bTstatistics.put(peerId, bTreqsInfo);
}

/**
 * Decode a string Python representation of transactions into an array of 
 * {@code Transaction} objects
 * @param value Set of transactions represented in a string
 * @param size Number of transactions
 * @param ip IP address of the BitTorrent peer related with these transactions
 * @param port Port number of the BitTorrent peer related with these transactions
 * @return Array of {@code Transaction} objects
 */
Transaction[] decodeTransactions(String value, int size, String ip) {
  String tranRegExpr = "\\[(\\((((\\d)*\\.(\\d)*|\\'[A-Z]*\\'|(\\d)*)\\,\\s)*((\\d)*"
      + "\\.(\\d)*|\\'[A-Z]*\\'|(\\d)*)\\)\\,\\s)*\\((((\\d)*\\.(\\d)*|\\'[A-Z]*\\'"
      + "|(\\d)*)\\,\\s)*((\\d)*\\.(\\d)*|\\'[A-Z]*\\'|(\\d)*)\\)\\]";
  if (!value.matches(tranRegExpr)) {
    System.err.println("ERROR :: Set of transactions has not the right format");
    return new Transaction[0];
  }
  Transaction[] transactions = new Transaction[size];
  int i = 0;
  int j;
  Scanner decoder = new Scanner(value).useDelimiter("\\[\\(|\\)\\,\\s\\(|\\)\\]");
  String str4tuple, item;
  Scanner tupleDecoder;
  String state = null;
  String type = null;
  String key;
  int pieceNumber = -1;
  // float kBprovided = -1.0f;
  Transaction tran;
  while (decoder.hasNext()) {
    str4tuple = decoder.next();
    tupleDecoder = new Scanner(str4tuple).useDelimiter(Pattern.compile("\\,\\s"));
    j = 0;
    while (tupleDecoder.hasNext()) {
      item = tupleDecoder.next();
      switch (j) {
      case 0:
        state = item;
        break;
      case 1:
        type = item;
        break;
      case 2:
        pieceNumber = Integer.valueOf(item);
        break;
      case 3:
        // kBprovided = Float.valueOf(item);
        break;
      default:
        System.out.println("This tuple has a not expected value");
        System.exit(1);
      }
      j++;
    }
    tupleDecoder.close();
    tran = new Transaction();
    tran.setState(identifyTransactionState(state));
    tran.setType(identifyTransactionType(type));
    tran.setItem(pieceNumber);
    key = ip;
    tran.setRemote(new PeerID(ip, wuwPorts.get(key)));
    // tran.setkBprovided(kBprovided);
    transactions[i] = tran;
    i++;
  }
  decoder.close();
  return transactions;
}

/**
 * Given an string, this method identifies the right {@code Transaction.State} enum 
 * object
 * @param state String representation of an enum {@code Transaction.State}
 * @return Associated enum object or {@code null} if the enum is not recognized
 */
Transaction.State identifyTransactionState(String state) {
  if (state.equals("'" + Transaction.State.DONE.toString() + "'")) 
    return Transaction.State.DONE;
  if (state.equals("'" + Transaction.State.ON.toString() + "'")) 
    return Transaction.State.ON;
  if (state.equals("'" + Transaction.State.WRONG.toString() + "'")) 
    return Transaction.State.WRONG;
  System.out.println("Transaction state not recognized");
  System.exit(1);
  return null;
}

/**
 * Given an string, this method identifies the right {@code Transaction.Type} enum 
 * object
 * @param type String representation of an enum {@code Transaction.Type}
 * @return Associated enum object or {@code null} if the enum is not recognized
 */
Transaction.Type identifyTransactionType(String type) {
  if (type.equals("'" + Transaction.Type.IN.toString() + "'")) return Transaction.Type.IN;
  if (type.equals("'" + Transaction.Type.OUT.toString() + "'")) return Transaction.Type.OUT;
  System.out.println("Transaction type not recognized");
  System.exit(1);
  return null;
}

private String[] decodeCurrentConnections(){
  int i = 0;
  String[] connections = new String[connectionsSize];
  Scanner decoder = new Scanner(connectionsStr).useDelimiter("\\[\\]|\\[|\\,\\s|\\]");
  while (decoder.hasNext()) {
    connections[i] = decoder.next();
    i++;
  }
  decoder.close();
  return connections;
}

/**
 * Decode the start-download times pair related with one piece of the content, in the 
 * BitTorrent context 
 * @return Array of Started and download times for each piece of the content
 */
private PieceDownTime[] decodeDowPiecesTimes() {
  // TODO Code a way for verifying the right format of pieces
  // String expecTuplRegExp =
  // "\\[(\\((((\\d)*|(\\d)*\\.(\\d)*|None)\\,\\s)*((\\d)*|(\\d)*\\.(\\d)*" +
  // "|None)\\)\\,\\s)*(\\((((\\d)*|(\\d)*\\.(\\d)*|None)\\,\\s)*((\\d)*|(\\d)*\\."
  // +
  // "(\\d)*|None)\\))\\]";
  // if( ! downPiecesValuesStr.matches(expecTuplRegExp) ){
  // System.out.println("This tuple (start-down piecestimes) has a not expected value");
  // System.exit(1);
  // }
  PieceDownTime[] downPiecesTimes = new PieceDownTime[downPiecesSize];
  int i = 0;
  int j;
  String str3tuple, item;
  Scanner tupleDecoder;
  Scanner decoder = new Scanner(downPiecesValuesStr).useDelimiter("\\[\\]|\\[\\(|\\)\\,\\s\\(|\\)\\]");
  while (decoder.hasNext()) {
    str3tuple = decoder.next();
    tupleDecoder = new Scanner(str3tuple).useDelimiter(Pattern.compile("\\,\\s"));
    j = 0;
    int piece = -1;
    float startTime = -1.0f;
    float endTime = -1.0f;
    while (tupleDecoder.hasNext()) {
      item = tupleDecoder.next();
      switch (j) {
      case 0:
        piece = Integer.valueOf(item);
        break;
      case 1:
        if (item.equals("None")) {
          startTime = -1.0f;
        } else {
          startTime = Float.valueOf(item);
        }
        break;
      case 2:
        if (item.equals("None")) {
          endTime = -1.0f;
        } else {
          endTime = Float.valueOf(item);
        }
        break;
      default:
        System.out.println("This tuple (start-end pieces times) has a not expected value");
        System.exit(1);
      }
      j++;
    }
    tupleDecoder.close();
    downPiecesTimes[i] = new PieceDownTime(piece, startTime, endTime);
    i++;
  }
  decoder.close();
  return downPiecesTimes;
}


float getSeconds() {
  return seconds;
}


String getContentId() {
  return contentId;
}


PieceDownTime[] getDowPiecesTimes() {
  return dowPiecesTimes;
}

String[] getConnectionIps(){
  return connectionIps;
}

/**
 * Get the {@code bTstatistics} attribute of this object. Pairs of each item in 
 * this map, is related to the BitTorrent peer ID as a key and the set of 
 * transactions ({@code BitTorrentRequest} object) as a value 
 * @return 
 */
HashMap<String, BitTorrentRequest> getbTstatistics() {
  return bTstatistics;
}

/**
 * Set the current content identifier related with a set of 
 * {@code Transaction} objects
 */
public void setLeftTransactionValues(){
  Iterator<BitTorrentRequest> values = bTstatistics.values().iterator();
  BitTorrentRequest btReq = null;
  int tranSize;
  while(values.hasNext()){
    btReq = values.next();
    tranSize = btReq.getTransactions().length;
    for(int i = 0; i < tranSize; i++)
      btReq.getTransactions()[i].setContentID(contentId);
  }
}

}