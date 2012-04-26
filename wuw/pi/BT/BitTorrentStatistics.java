package wuw.pi.BT;

import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import wuw.pi.Transaction;


/**
 * The BitTorrentStatistics class lets to decode a python dictionary (data
 * structure that contains BitTorrent statistics for WUW core) into Java data
 * types, for each attribute in this class. For getting a Python dictionary, it
 * is mandatory to make a local Java socket connection (port 5000) to a
 * BitTorrent instance.
 * 
 * @author gomez-r
 * @version 1.0
 */
public class BitTorrentStatistics {

/*
 * These attributes are useful for decode process.
 */
@SuppressWarnings("serial")
private HashMap<String, String> expectedValues = new HashMap<String, String>() {

  {
    put("s", "s");
    put("cid", "cid");
    put("dp", "dp");
    put("dps", "dps");
  }
};

/*
 * These attributes are measures of the BitTorrent performance.
 */
private float seconds;
private String contentId = null;
private int downPiecesSize;
private String downPiecesValuesStr = null;
private PieceDowTime[] dowPiecesTimes = null;
private HashMap<String, BitTorrentRequest> bTstatistics = new HashMap<String, BitTorrentRequest>();


/**
 * Once this constructor is called, the Python dictionary will be decode.
 * 
 * @param source
 *          String representation of the Python dictionary
 */
public BitTorrentStatistics(String source) {
  /*
   * Scanner class with the specified delimiter lets to get an easy handle way
   * for keys and values in the Python dictionary.
   */
  Scanner decoder = new Scanner(source).useDelimiter("\\,\\s\\'\\_|\\'\\:\\s|\\{\\'\\_|\\}");
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
          contentId = value;
        }
        if (key.equals("dps")) {
          downPiecesSize = Integer.valueOf(value);
        }
        if (key.equals("dp")) {
          downPiecesValuesStr = value;
        }
      } else {
        if (this.hasExpectFormat(key)) {
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
}


private boolean hasExpectFormat(String key) {
  String[] valuesInKey = key.split("\\_");
  if (valuesInKey.length != 7) {
    System.out.println("The number of values in key is incorrect");
    return false;
  }
  String ipAddress = valuesInKey[0];
  if (!ipAddress.matches("((\\d)*\\.)*(\\d)*")) {
    System.out.println("Ip address has not a rigth format");
    return false;
  }
  String bitTorrentId = valuesInKey[1];
  if (!bitTorrentId.matches("(\\d|[a-zA-Z]|\\-)*")) {
    System.out.println("BitTorrent identifier has not a right format");
    return false;
  }
  String regularExprForFloat = "(\\d)*\\.(\\d)*|(\\d)*";
  String currUplBan = valuesInKey[2];
  if (!currUplBan.matches(regularExprForFloat)) {
    System.out.println("Expected float value (currUplB) is not right");
    return false;
  }
  String currDowBand = valuesInKey[3];
  if (!currDowBand.matches(regularExprForFloat)) {
    System.out.println("Expected float value (currDowB) is not right");
    return false;
  }
  String maxUplBand = valuesInKey[4];
  if (!maxUplBand.matches(regularExprForFloat)) {
    System.out.println("Expected float value (maxUplB) is not right");
    return false;
  }
  String maxDowBand = valuesInKey[5];
  if (!maxDowBand.matches(regularExprForFloat)) {
    System.out.println("Expected float value (dowUplB) is not right");
    return false;
  }
  String transactions = valuesInKey[6];
  if (!transactions.matches(regularExprForFloat)) {
    System.out.println("Expected float value (transactions) is not right");
    return false;
  }
  return true;
}


private void decodeBitTorrentReqs(String key, String value) {
  String[] valuesInKey = key.split("\\_");
  String ipAddress = valuesInKey[0];
  String bitTorrentId = valuesInKey[1];
  float currUplBan = Float.valueOf(valuesInKey[2]);
  float currDowBand = Float.valueOf(valuesInKey[3]);
  float maxUplBand = Float.valueOf(valuesInKey[4]);
  float maxDowBand = Float.valueOf(valuesInKey[5]);
  int transactions = Integer.valueOf(valuesInKey[6]);
  Transaction[] transactionArr = decodeTransactions(value, transactions);
  BitTorrentRequest bTreqsInfo = new BitTorrentRequest();
  bTreqsInfo.setIpAddr(ipAddress);
  bTreqsInfo.setBitTorrentId(bitTorrentId);
  bTreqsInfo.setCurrentUplBandwidth(currUplBan);
  bTreqsInfo.setCurrentDowBandwidth(currDowBand);
  bTreqsInfo.setMaxUplBandwidth(maxUplBand);
  bTreqsInfo.setMaxDowBandwidth(maxDowBand);
  bTreqsInfo.setTransactions(transactionArr);
  String peerId = ipAddress + bitTorrentId;
  bTstatistics.put(peerId, bTreqsInfo);
}


private Transaction[] decodeTransactions(String value, int size) {
  String tranRegExpr = "\\[(\\((((\\d)*\\.(\\d)*|\\'[A-Z]*\\'|(\\d)*)\\,\\s)*((\\d)*"
      + "\\.(\\d)*|\\'[A-Z]*\\'|(\\d)*)\\)\\,\\s)*\\((((\\d)*\\.(\\d)*|\\'[A-Z]*\\'"
      + "|(\\d)*)\\,\\s)*((\\d)*\\.(\\d)*|\\'[A-Z]*\\'|(\\d)*)\\)\\]";
  if (!value.matches(tranRegExpr)) {
    System.out.println("Set of transactions is not well formatted");
    System.exit(1);
  }
  Transaction[] transactions = new Transaction[size];
  int i = 0;
  int j;
  Scanner decoder = new Scanner(value).useDelimiter("\\[\\(|\\)\\,\\s\\(|\\)\\]");
  String str4tuple, item;
  Scanner tupleDecoder;
  String state = null;
  String type = null;
  int pieceNumber = -1;
  float kBprovided = -1.0f;
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
        kBprovided = Float.valueOf(item);
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
    // tran.setkBprovided(kBprovided);
    transactions[i] = tran;
    i++;
  }
  decoder.close();
  return transactions;
}


private Transaction.State identifyTransactionState(String state) {
  if (state.equals("'" + Transaction.State.DONE.toString() + "'")) return Transaction.State.DONE;
  if (state.equals("'" + Transaction.State.ON.toString() + "'")) return Transaction.State.ON;
  if (state.equals("'" + Transaction.State.WRONG.toString() + "'")) return Transaction.State.WRONG;
  System.out.println("Transaction state not recognized");
  System.exit(1);
  return null;
}


private Transaction.Type identifyTransactionType(String type) {
  if (type.equals("'" + Transaction.Type.IN.toString() + "'")) return Transaction.Type.IN;
  if (type.equals("'" + Transaction.Type.OUT.toString() + "'")) return Transaction.Type.OUT;
  System.out.println("Transaction type not recognized");
  System.exit(1);
  return null;
}


private PieceDowTime[] decodeDowPiecesTimes() {
  // String expecTuplRegExp =
  // "\\[(\\((((\\d)*|(\\d)*\\.(\\d)*|None)\\,\\s)*((\\d)*|(\\d)*\\.(\\d)*" +
  // "|None)\\)\\,\\s)*(\\((((\\d)*|(\\d)*\\.(\\d)*|None)\\,\\s)*((\\d)*|(\\d)*\\."
  // +
  // "(\\d)*|None)\\))\\]";
  // if( ! downPiecesValuesStr.matches(expecTuplRegExp) ){
  // System.out.println("This tuple (start-down piecestimes) has a not expected value");
  // System.exit(1);
  // }
  PieceDowTime[] downPiecesTimes = new PieceDowTime[downPiecesSize];
  int i = 0;
  int j;
  String str3tuple, item;
  Scanner tupleDecoder;
  Scanner decoder = new Scanner(downPiecesValuesStr).useDelimiter("\\[\\(|\\)\\,\\s\\(|\\)\\]");
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
    downPiecesTimes[i] = new PieceDowTime(piece, startTime, endTime);
    i++;
  }
  decoder.close();
  return downPiecesTimes;
}


public float getSeconds() {
  return seconds;
}


public String getContentId() {
  return contentId;
}


public PieceDowTime[] getDowPiecesTimes() {
  return dowPiecesTimes;
}


public HashMap<String, BitTorrentRequest> getbTstatistics() {
  return bTstatistics;
}

}