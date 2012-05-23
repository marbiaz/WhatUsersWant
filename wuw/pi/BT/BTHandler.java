package wuw.pi.BT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import wuw.core.PeerID;
import wuw.pi.PIHandler;
import wuw.pi.Transaction;


public class BTHandler implements PIHandler {
	
private PeerID[] wuwPeerList = null;

public BTHandler(PeerID[] peerList){
	// TODO Initiate fake tracker in order to handle: messages from/to the local BT client
	// (announce messages), build answers to/from the real tracker.
	// In each torrent file replace real trcker's URL by fake tracker's URL
	// Create a Singleton for handling ranked peerList. Wuw core stuff and Tracker service will
	// get access to the singleton object
	wuwPeerList = peerList;
}

	
@Override
public Transaction[] giveContentUpdates() {
  String FromServer = "";
  Socket clientSocket;
  try {
    clientSocket = new Socket("localhost", 5000);
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
        clientSocket.getInputStream()));
    FromServer = inFromServer.readLine();
    clientSocket.close();
  }
  catch (UnknownHostException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  System.out.println(FromServer);
  BitTorrentStatistics bTstatistics = new BitTorrentStatistics(FromServer);
  bTstatistics.setLeftTransactionValues(wuwPeerList);
  Iterator<Entry<String, BitTorrentRequest>> it = bTstatistics.getbTstatistics()
      .entrySet().iterator();
  Entry<String, BitTorrentRequest> e;
  ArrayList<Transaction> res = new ArrayList<Transaction>();
  while (it.hasNext()) {
    e = it.next();
    res.addAll(Arrays.asList(e.getValue().getTransactions()));
  }
  return res.toArray(new Transaction[0]);
}


@Override
public void getPeers(String contentID, PeerID[] peers) {
  // TODO Auto-generated method stub
  
}

}
