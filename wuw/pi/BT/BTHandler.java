package wuw.pi.BT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import simple.http.connect.ConnectionFactory;
import simple.http.load.MapperEngine;
import simple.http.serve.Context;
import simple.http.serve.FileContext;

import wuw.core.Config;
import wuw.core.PeerID;
import wuw.pi.PIHandler;
import wuw.pi.Transaction;
import wuw.pi.BT.btutils.TorrentFile;
import wuw.pi.BT.btutils.TorrentProcessor;

/**
 * This class communicates a BitTorrent client with WUW. Basically, this class 
 * receives information about pieces interchange (in which each P2P client is 
 * involved on) and sends list of peers to the local P2P client (basic implementation 
 * of a BitTorrent tracker)
 * 
 * @author carvajal-r
 *
 */
public class BTHandler implements PIHandler {

// Set of leechers and seeders in the P2P overlay
private HashMap<String, BtWuwPeer> btLeechers;
private HashMap<String, BtWuwPeer> btSeeders;
// Set of WUW instances
private HashMap<String, Integer> wuwPeers;
private String[] bestRankedKeys;
private Object updateLock = new Object();

public BTHandler(String peerListFilePath){
  btLeechers = new HashMap<String, BtWuwPeer>();
  btSeeders = new HashMap<String, BtWuwPeer>();
  wuwPeers = new HashMap<String, Integer>();
  loadWuwBtPeerList(peerListFilePath);
  // Before the Tracker Emulator begins, the URL in each torrent file (in which each 
  // BitTorrent client is involved) must be change
  final Thread trackerEmulator = new Thread(new Runnable() {
    public void run() {
      changeRealTrackerUrl();
      startFakeTracker(System.currentTimeMillis(), 
          Integer.parseInt(Config.getValue("config","duration")));
    }
  });
  trackerEmulator.start();
}

/*
 * (non-Javadoc)
 * @see wuw.pi.PIHandler#giveContentUpdates()
 */
@Override
public Transaction[] giveContentUpdates() {
  // For getting a Python dictionary, the current implementation makes a local 
  // Java socket connection (port 5000) to a BitTorrent instance.
  String FromServer = null;
  Socket clientSocket;
  try {
    clientSocket = new Socket(Config.getValue("bittorrent", "bitTorrentHost"),
        Integer.parseInt(Config.getValue("bittorrent","bitTorrentPort")));
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
        clientSocket.getInputStream()));
    FromServer = inFromServer.readLine();
    clientSocket.close();
  }
  catch (UnknownHostException e) {
    e.printStackTrace();
  }
  catch (IOException e) {
    e.printStackTrace();
  }
  if(FromServer == null)
    FromServer = "{}";
  System.out.println("Received dictionary from BitTorrent :: ");
  System.out.println(FromServer);
  BitTorrentStatistics bTstatistics = new BitTorrentStatistics(FromServer, wuwPeers);
  bTstatistics.setLeftTransactionValues();
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

/*
 * (non-Javadoc)
 * @see wuw.pi.PIHandler#getPeers(java.lang.String, wuw.core.PeerID[])
 */
@Override
public void getPeers(String contentID, PeerID[] peers) {
  // TODO extend the case for different contents ID's
  int length;
  if(peers.length == 0){
    bestRankedKeys = null;
  }else{
    synchronized(updateLock){
      length = peers.length;
      bestRankedKeys = new String[length];
      for(int i = 0; i < length; i++){
        bestRankedKeys[i] = peers[i].toString();
      }
    }
  }
}

/*
 * (non-Javadoc)
 * @see wuw.pi.PIHandler#getBestRankedPeers()
 */
@Override
public BtWuwPeer[] getBestRankedPeers(){
  String[] keys;
  int length;
  BtWuwPeer[] bestPeers;
  synchronized(updateLock){
    if(bestRankedKeys == null)
      return null;
    length = bestRankedKeys.length;
    keys = new String[length];
    for(int i = 0; i < length; i++)
      keys[i] = bestRankedKeys[i];
  }
  bestPeers = new BtWuwPeer[keys.length];
  for(int i = 0; i < keys.length; i++){
    if(btSeeders.containsKey(keys[i]))
      bestPeers[i] = (BtWuwPeer) btSeeders.get(keys[i]);
    else
      bestPeers[i] = (BtWuwPeer) btLeechers.get(keys[i]);
  }
  return bestPeers;
}

/**
 * Load from a file the set of WUW instances and BitTorrent peers in the overlay. 
 * This is a easy way to have a global knowledge of the overlay, but it is not 
 * realistic
 * @param filePath Full path of the text file 
 */
private void loadWuwBtPeerList(String filePath){
  BtWuwPeer peer;
  int btPort, wuwPort;
  BufferedReader buf;
  String[] tokens;
  String key, ipAddr, peerType, line;
  try {
    buf = new BufferedReader(new FileReader(filePath));
    while( (line = buf.readLine()) != null ){
      tokens = line.split("[\\s]+");
      if (tokens[0].startsWith("peer.", 0)){
        ipAddr = tokens[1];
        wuwPort = Integer.valueOf(tokens[2]);
        btPort = Integer.valueOf(tokens[3]);
        wuwPeers.put(ipAddr, wuwPort);
        peerType = tokens[4];
        key = ipAddr + ":" + wuwPort;
        peer = new BtWuwPeer(ipAddr, wuwPort, btPort);
        if(peerType.equalsIgnoreCase("LEECHER"))
          btLeechers.put(key, peer);
        if(peerType.equalsIgnoreCase("SEEDER"))
          btSeeders.put(key, peer);
      }
    }
  }
  catch (Exception e) {
    System.err.println("Configuration : error while reading peer list input file.");
    e.printStackTrace();
  }
  ipAddr = Config.getValue("localpeer", "ipAddress");
  wuwPort = Integer.valueOf(Config.getValue("localpeer", "portNumber"));
  key = ipAddr + ":" + wuwPort;
  if( btLeechers.containsValue(key) )
    btLeechers.remove(key);
  if( btSeeders.containsKey(key) )
    btSeeders.remove(key);
}

/*
 * (non-Javadoc)
 * @see wuw.pi.PIHandler#getPeersForAnnounce()
 */
@Override
public BtWuwPeer[] getPeersForAnnounce(){
  if(btSeeders.size() == 0 && btLeechers.size() == 0){
    System.err.println("There is any seeder and leecher in the peer list file");
    return null;
  }
  int i = 0;
  int seedersNum = 0;
  int leechersNum = 0;
  BtWuwPeer[] peers, seeders = null, leechers = null;
  ArrayList<BtWuwPeer> tempList;
  @SuppressWarnings("rawtypes")
  Iterator peerIt;
  if(btSeeders.size() != 0){
    seedersNum = (int) Math.ceil( (3.0 * btSeeders.size()) / 10.0 );
    tempList = new ArrayList<BtWuwPeer>(btSeeders.size());
    peerIt = btSeeders.values().iterator();
    while(peerIt.hasNext())
      tempList.add((BtWuwPeer)peerIt.next());
    seeders =  choseRandomly(tempList.toArray(new BtWuwPeer[0]), seedersNum);
  }
  if(btLeechers.size() != 0){
    leechersNum = (int) Math.ceil( btLeechers.size() / 10.0);
    tempList = new ArrayList<BtWuwPeer>(btLeechers.size());
    peerIt = btLeechers.values().iterator();
    while(peerIt.hasNext())
      tempList.add((BtWuwPeer) peerIt.next());
    leechers = choseRandomly(tempList.toArray(new BtWuwPeer[0]), leechersNum);
  }
  peers = new BtWuwPeer[leechersNum + seedersNum];
  if(seeders != null){
    for(int j = 0; j < seeders.length; j++){
      peers[i] = seeders[j]; 
      i++;
    }
  }
  if(leechers != null){
    for(int j = 0; j < leechers.length; j++){
      peers[i] = leechers[j];
      i++;
    }
  }
  return peers;
}

private static BtWuwPeer[] choseRandomly(BtWuwPeer[] source, int items){
  BtWuwPeer[] result = new BtWuwPeer[items];
  if(items == 1){
    result[0] = source[0];
    return result;
  }
  int[] temp = new int[source.length];
  int ran;
  for (int i = 0; i < items;) {
    ran = Config.rand.nextInt(source.length);
    if (temp[ran] == 0) {
      temp[ran] = 1;
      result[i] = source[ran];
      i++;
    }
  }
  return result;
}

/**************************** TRACKER EMULATOR METHODS *******************************/
/**
 * Replace the tracker's URL written in all torrent files in the torrents file 
 * directory (specified in XML configuration file). This method does not check if 
 * {@code newUrl} and {@code torrentsPath} are well formatted. 
 * @param torrentsPath Torrent files directory's full path 
 * @param newUrl New tracker's URL string
 */
@SuppressWarnings("rawtypes")
public void changeRealTrackerUrl(){
  File torrentsDir = new File(Config.getValue("faketracker", "torrentsDir"));
  Map torrentParser;
  TorrentFile newTorrentFile;
  String fileName, fakeTrackerUrl;
  TorrentProcessor tp;
  FileOutputStream fos;
  fakeTrackerUrl = "http://" + Config.getValue("faketracker", "ipAddress")
      + ":" + Config.getValue("faketracker", "portNumber") + "/announce";
  for( File oldTorrentFile : torrentsDir.listFiles() ){
    torrentParser = new TorrentProcessor().parseTorrent(oldTorrentFile);
    newTorrentFile = new TorrentProcessor().getTorrentFile(torrentParser);
    fileName = oldTorrentFile.getName();
    oldTorrentFile.delete();
    tp = new TorrentProcessor(newTorrentFile);
    tp.setAnnounceURL(fakeTrackerUrl);
    try {
      fos = new FileOutputStream(Config.getValue("faketracker", "torrentsDir")
          + "/" +fileName);
      fos.write(tp.generateTorrent());

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

/**
 * Modification of the Java Bittorrent API (its name indicates is a JAVA API 
 * that implements the Bittorrent Protocol; more information about Java 
 * Bittorrent API in http://sourceforge.net/projects/bitext/) for WUW. This 
 * method lunch a basic BitTorrent Tracker that answers with peer lists all 
 * BitTorrent requests, through HTTP messages   
 * @param start
 * @param duration
 */
public void startFakeTracker(long start, int duration){
  try {
    FileWriter fw = new FileWriter(Config.getValue("faketracker", "context") +
        "/Mapper.xml");
    fw.write("<?xml version=\"1.0\"?>\r\n<mapper>\r\n<lookup>\r\n" +
        "<service name=\"tracker\" type=\"wuw.pi.BT.btutils.TrackerService\"/>\r\n" +
        "</lookup>\r\n<resolve>\r\n" +
        "<match path=\"/announce*\" name=\"tracker\"/>\r\n" +
        "</resolve>\r\n</mapper>");
    fw.flush();
    fw.close();
  } catch (IOException ioe) {
    System.err.println("Could not create 'Mapper.xml'");
    ioe.printStackTrace();
    System.exit(0);
  }
  Context context = new FileContext(new File(Config.getValue("faketracker", "context")));
  try {
    MapperEngine engine = new MapperEngine(context);
    ServerSocket sock = new ServerSocket(
        Integer.parseInt(Config.getValue("faketracker", "portNumber")));
    ConnectionFactory.getConnection(engine).connect(sock);
    System.out.println(
        "WUW Tracker Emulator started! Listening on port :: " + 
            Config.getValue("faketracker", "portNumber"));
  } catch (Exception e) {
    e.printStackTrace();
    System.err.println("Problem during the initialization of the tracker emulator service");
    System.exit(1);
  }
  while ((System.currentTimeMillis() - start) < duration) {
    try {
      Thread.sleep(duration);
    }
    catch (InterruptedException e) {
      System.err.println("Tracker Emulator thread: unexpected interruption while " +
          "waiting for task completion...");
      e.printStackTrace();
      System.err.println("Tracker Emulator thread: going back to sleep...");
    }
  }
  System.out.println("Tracker Emulator is down...");
}

}
