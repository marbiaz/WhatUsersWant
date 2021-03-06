/**
 * Peer.java
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

package wuw.core;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

import wuw.comm.CommHandler;
import wuw.comm.newscast.Newscast;
import wuw.pi.Transaction;
import wuw.pi.PIHandler;
import wuw.ui.UIHandler;


/**
 * An instance of this class will contain all that belongs to the local peer.
 * 
 * @author Marco Biazzini
 * @date 2012 January 20
 */
public class Peer {


private class RankedPeer implements Comparable<Object> {

PeerID peer;
String[] contentIDs;
double score;
int contents;

RankedPeer(PeerID p, int conts) {
  peer = p;
  contentIDs = new String[conts];
  contents = 0;
  score = 0;
  Arrays.fill(contentIDs, null);
}


void addContent(String c) {
  contentIDs[contents] = c;
  contents++;
}


/*
 * (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o) {
  if (o instanceof RankedPeer) {
    return ((RankedPeer)o).score == this.score;
  }
  return ((PeerID)o).equals(this.peer);
}


/* (non-Javadoc)
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
@Override
public int compareTo(Object o) {
  if (o instanceof PeerID) {
    return this.peer.compareTo((PeerID)o);
  }
  double res = this.score - ((RankedPeer)o).score;
  if (res < 0.0) {
    return -1;
  }
  if (res > 0.0) {
    return 1;
  }
  return 0;
}

}

private boolean printLogs;

public static final String PAS_A = UIHandler.feedback_measures[0];
public static final String PAS_S = UIHandler.feedback_measures[1];
public static final String PAS_E = UIHandler.feedback_measures[2];
public static final String PAC_A = UIHandler.feedback_measures[3];
public static final String PAC_S = UIHandler.feedback_measures[4];
public static final String PAC_E = UIHandler.feedback_measures[5];

static final boolean sharePrefs = Boolean.parseBoolean(
    Config.getValue("localpeer","sharePrefs"));
static final boolean shareInts = Boolean.parseBoolean(
    Config.getValue("localpeer","shareInts"));
static final boolean saveAll = Boolean.parseBoolean(
    Config.getValue("localpeer","saveAll"));
static final double alpha = Double.parseDouble(
    Config.getValue("localpeer", "alpha"));
static final double pref_weight = Double.parseDouble(
    Config.getValue("localpeer", "pref_weight"));
static final double selfishness = Double.parseDouble(
    Config.getValue("localpeer", "selfishness"));
static final int maxNeighSize = Integer.parseInt(
    Config.getValue("localpeer","maxNeighSize"));

private final CommHandler transport;
private final UIHandler ui;
private final PIHandler pi;
private final Newscast epidemic;
final PeerID localID;
private final ContentBasket myContents;
private final Neighborhood globalNeighborhood;
private ArrayList<PeerDescriptor> epidemicUpdates;
private Timer computer;
private int contents;
private final Object updateLock, dLock;
private PeerDescriptor myDescriptor;

/*
 * Standard constructor. It should not be called but by the {@link
 * wuw.core.Config#set(String[])} method.
 * 
 * @param p The {@link PeerID} of the local peer
 * 
 * @param t The {@link CommHandler} object associated with the local peer
 */
Peer(PeerID p, CommHandler t, UIHandler ui, PIHandler pi, Newscast n) {
  localID = p;
  transport = t;
  epidemic = n;
  contents = 0;
  this.ui = ui;
  this.pi = pi;
  myContents = new ContentBasket();
  globalNeighborhood = new Neighborhood();
  epidemicUpdates = new ArrayList<PeerDescriptor>(Integer.parseInt(
      Config.getValue("localpeer","queueSize")));
  printLogs = Config.printLogs;
  updateLock = new Object();
  dLock = new Object();
  myDescriptor = new PeerDescriptor();
  computer = new Timer(Integer.parseInt(Config.getValue("localpeer","doTheMagicTimer")),
      new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      if (contents > 0) doTheMagic();
    }
  });
  computer.start();
}

/**
 * @return The ID of the local peer
 */
public PeerID getPeerID() {
  return localID;
}

/**
 * @return The {@link CommHandler} object associated with the local peer
 */
public CommHandler getTransport() {
  return transport;
}

/**
 * It returns the local peer's freshest {@link PeerDescriptor} available.
 * This method does not trigger any computation of intentions or scores.
 * It simply picks up and returns the descriptor that as been computed
 * in the latest iteration of the evaluation routine of WUW.
 * @return The up-to-date {@link PeerDescriptor} of the local peer.
 */
public Object getDescriptor() {
  Object res;
  synchronized (dLock) {
    res = myDescriptor;
  }
  return res;
}


/**
 * Add a content to the content list of the local peer. If a content with the
 * same ID is already in the list, no addition occurs. Adding a content also
 * adds the neighbors associated with the {@link ContentData} to local peers'
 * neighborhood. The new neighbors, though, will not be taken into account to
 * compute feedback values until the local peer gets to know some information
 * about them (by means of either epidemic message exchange or local P2P
 * application's data).
 * 
 * @param c
 *          The content to add.
 * @return <code>true</code> if the given content is added; <code>false</code>
 *         if it was already in the list.
 */
public boolean addContent(String id, int items, Map<String, String> preferences, PeerID[] peerList) {
  PeerID remote;
  Neighbor n;
  LocalContentData c;
  boolean res = false;
  int i, k = 0, l = peerList.length;
  int j = l - 1;
  long now = System.currentTimeMillis();
  Neighbor[] neighs = null;
  synchronized (updateLock) {
    c = myContents.getContent(id);
    if (c == null) {
      c = new LocalContentData(id, items, preferences);
      contents = myContents.addContent(c);
      ui.initFeedback(id, UIHandler.feedback_measures, UIHandler.feedback_defaults);
      res = true;
      neighs = new Neighbor[l];
      for (i = 0; i < l; i++) {
        remote = peerList[i];
        n = globalNeighborhood.getNeighbor(remote);
        if (n == null) {
          n = new Neighbor(remote, now);
          globalNeighborhood.addNeighbor(n);
          neighs[k++] = n;
        } else {
          neighs[j--] = n;
        }
        n.addContent(c);
      }
      c.init(neighs);
      makeDescriptor();
    }
  }
  if (myDescriptor.isValid()) {
    // XXX: use neighs.length instead of k, to choose among already known
    // peers as well ...
    int chosen = k > 0 ? Config.rand.nextInt(k) : 0; // send at least 1 msg
    for (i = 0; i < k; i++) {
      if (i == chosen || Config.rand.nextDouble() < (Math.log(k) / k)) {
        this.epidemic.sendCard(myDescriptor, neighs[i].ID);
      }
    }
  } else if (k > 0) {
/**/System.err
        .println("Peer: ATTENTION: local peer's card not sent due to previous errors while adding a new content.");
    System.err.flush();
  }
  return res;
}


/**
 * Get the {@link PeerDescriptor}s updates from the overlay manager.
 * 
 * @param upd
 *          Descriptors to get
 */
public void getEpidemicUpdates(Object upd[]) {
  int indx, i = 0;
  PeerDescriptor  pd;
  synchronized (epidemicUpdates) {
    for (i = 0; i < upd.length; i++) {
      pd = (PeerDescriptor)upd[i];
      indx = Collections.binarySearch(epidemicUpdates, pd);
      if (indx >= 0 && epidemicUpdates.get(indx).getVersion() < pd.getVersion()) {
        epidemicUpdates.set(indx, pd);
      } else if (indx < 0) {
        epidemicUpdates.add(-indx - 1, pd);
      }
    }
  }
  if (printLogs) {
    if(epidemicUpdates.size() > Integer.parseInt(
        Config.getValue("localpeer","queueSize"))){
      System.err.println("Peer: ATTENTION: Epidemic updates queue congestion!");
      System.err.flush();
    }
    System.out.println("Peer : The epidemic updates queue currently contains "
        + epidemicUpdates.size() + " descriptors.");
  }
}


private void makeDescriptor() {
  synchronized (dLock) {
    if (contents > 0) {
      ContentData[] c = myContents.toArray();
      myDescriptor = new PeerDescriptor(localID, c);
    } else {
      myDescriptor = new PeerDescriptor();
    }
  }
}


/*
 * It simply returns the average of the linear combination between
 * local and remote peer's intentions both as pas and as pac.
 */
private double scoreNeigh(Intention myIntent, Intention nIntent) {
  double res;
  res = pref_weight * myIntent.pacIntent + (1 - pref_weight) * nIntent.pacIntent;
//  res += myIntent.pasIntent + nIntent.pacIntent;
  // res = (selfishness * myIntent.pacIntent) + ((1 - selfishness) * nIntent.pasIntent);
  // res += (selfishness * myIntent.pasIntent) + ((1 - selfishness) * nIntent.pacIntent);
  return res;
}


/*
 * The global ranking is build by taking into account the intentions for/of each
 * neighbor for each content the local peer is trading.
 * Every neighbor is scored, applying the scoreNeigh function, for each content
 * it is trading with the local peer. These scores sum up and then
 * the average (global score) is taken for each neighbor. Then the list of all 
 * scored neighbor is sorted by global score. Finally the best maxNeighSize peers
 * for each content are given to the P2P application.
 */
private void buildGlobalRanking() {
  ArrayList<Intention> intents;
  Intention nintent;
  int pos, csize = myContents.size();
  String cIDs[] = myContents.getIDs(), strLog = "'rankedPeers': {";
  RankedPeer curr;
  ArrayList<PeerID> neighborhood;
  HashMap<String, ArrayList<PeerID>> neighborhoods =
      new HashMap<String, ArrayList<PeerID>>(csize * 2);
//Map useful for logging best ranked peers with scores
  HashMap<String, ArrayList<RankedPeer>> rankedPeersWithScores = 
      new HashMap<String, ArrayList<RankedPeer>>(csize*2);
//Item in rankedPeersWithScores map
  ArrayList<RankedPeer> rankedPeers;
//Temp ranked peer
  RankedPeer[] rankedArr = null;
  ArrayList<RankedPeer> globalRanking = new ArrayList<RankedPeer>(globalNeighborhood.size());
  PeerID[] bestRanked = null;
  for (int i = 0; i < csize; i++) {
    intents = myContents.getContent(cIDs[i]).intentions;
    for (Intention intent : intents) {
      pos = Collections.binarySearch(globalRanking, intent.remote.ID);
      if (pos < 0) {
        curr = new RankedPeer(intent.remote.ID, csize);
        globalRanking.add(-pos - 1, curr);
      } else {
        curr = globalRanking.get(pos);
      }
      curr.addContent(cIDs[i]);
      nintent = intent.remote.getContent(cIDs[i]).contentInfo.intentions.get(0);
      curr.score += scoreNeigh(intent, nintent);
    }
  }
  for (int i = 0; i < globalRanking.size(); i++) {
    curr = globalRanking.get(i);
    curr.score /= curr.contents;
  }
  Collections.sort(globalRanking);
  for (int i = globalRanking.size() -1; i >= 0; i--) {
    curr = globalRanking.get(i);
    for (String c : curr.contentIDs) {
      if (neighborhoods.containsKey(c)) {
        neighborhood = neighborhoods.get(c);
      } else {
        neighborhood = new ArrayList<PeerID>(maxNeighSize);
        neighborhoods.put(c, neighborhood);
      }
      if (neighborhood.size() < maxNeighSize) {
        neighborhood.add(curr.peer);
      }
      if(rankedPeersWithScores.containsKey(c))
        rankedPeers = rankedPeersWithScores.get(c);
      else{
        rankedPeers = new ArrayList<RankedPeer>(maxNeighSize);
        rankedPeersWithScores.put(c, rankedPeers);
      }
      if(rankedPeers.size() < maxNeighSize)
        rankedPeers.add(curr);
    }
  }
  Entry<String, ArrayList<PeerID>> e;
  Iterator<Entry<String, ArrayList<PeerID>>> nIt = neighborhoods.entrySet().iterator();
  while (nIt.hasNext()) {
    e = nIt.next();
    bestRanked = e.getValue().toArray(new PeerID[0]);
    pi.getPeers(e.getKey(), bestRanked);
  }
  // Adding to log the best ranked peers
  Entry<String, ArrayList<RankedPeer>> rE;
  Iterator<Entry<String, ArrayList<RankedPeer>>> rIt = rankedPeersWithScores.entrySet().iterator();
  while(rIt.hasNext()){
    rE = rIt.next();
    rankedArr = rE.getValue().toArray(new RankedPeer[0]);
    if(rankedArr != null){
      for(int i = 0; i < rankedArr.length; i++){
        if( i == rankedArr.length - 1 )
          strLog += "'" + rankedArr[i].peer.ip.getHostAddress() + "': " + rankedArr[i].score;
        else
          strLog += "'" + rankedArr[i].peer.ip.getHostAddress() + "': " + rankedArr[i].score + ", ";
      }
    }
  }
  strLog += "}";
  Config.logger.updateLogLine(strLog);
}


private void doTheMagic() {

  int i, j, c, dl, tl;
  String conts[], peerIps[], strLog = "";
  //long lastTime, actuTime;
  //lastTime = System.currentTimeMillis();
  PeerDescriptor[] newDescriptors = new PeerDescriptor[0];
  synchronized (epidemicUpdates) {
	  newDescriptors = epidemicUpdates.toArray(newDescriptors);
	  epidemicUpdates.clear();
  }
  Transaction[] newTrans = pi.giveContentUpdates();
  peerIps = pi.getCurrentPeerConnections();
  if (newTrans == null) {
    newTrans = new Transaction[0];
  } else if (newTrans.length > 1) {
    Arrays.sort(newTrans);
  }
  // Adding to log current peer connections
  strLog += "{'connections': [";
  if( peerIps != null){
    for(int k = 0; k < peerIps.length; k++){
      if( k == peerIps.length - 1 )
        strLog += peerIps[k];
      else
        strLog += peerIps[k] + ", ";
    }
  }
  strLog += "], ";
  Config.logger.updateLogLine(strLog);
// Adding to log current descriptors and transactions
//  if(newDescriptors != null){
//    strLog += "'descriptors': [";
//    for(int k = 0; k < newDescriptors.length; k++){
//      if( k == newDescriptors.length - 1 )
//        strLog += newDescriptors[k].toString();
//      else
//        strLog += newDescriptors[k].toString() + ", ";
//    }
//    strLog += "], 'transactions': [";
//    for(int k = 0; k < newTrans.length; k++){
//      if( k == newTrans.length - 1 )
//        strLog += newTrans[k].toString();
//      else
//        strLog += newTrans[k].toString() + ", ";
//    }
//    strLog += "], ";
//    Config.logger.updateLogLine(strLog);
//  }
/**/if (printLogs) {
  System.out.println("New transactions :\n" + Config.printArray(newTrans));
  }

  PeerID id;
  Neighbor n;
  PeerDescriptor pd = null;
  Transaction t = null;
  LocalContentData cd = null;
  String cid;
  boolean goT, goD, nowD, updated, newBuddy, erased = false;
  long now = System.currentTimeMillis();
  ArrayList<PeerID> newNeighs = new ArrayList<PeerID>();
  ArrayList<String> updates = new ArrayList<String>();
  i = 0;
  j = 0;
  n = null;
  dl = newDescriptors.length;
  tl = newTrans.length;
  goT = tl > 0;
  goD = dl > 0;

  synchronized (updateLock) {
    conts = myContents.getIDs();
    while (goD || goT) {
      updated = false;
      newBuddy = false;
      pd = goD ? newDescriptors[i] : pd;
      t = goT ? newTrans[j] : t;
      c = goD ? goT ? pd.getPeerID().compareTo(t.getRemote()) : -1 : 1;
      nowD = c <= 0; // !goT || (goD && c <= 0);
      if (nowD) {
        id = pd.getPeerID();
        if ((i + 1) < dl)
          i++;
        else
          goD = false;
      } else { // if goT && (!goD || c > 0)
        id = t.getRemote();
        if ((j + 1) < tl)
          j++;
        else
          goT = false;
      }
      if (n == null || !n.ID.equals(id)) {
        n = globalNeighborhood.getNeighbor(id);
        erased = false;
      }
      if (n == null) {
        n = new Neighbor(id, now);
        newBuddy = globalNeighborhood.addNeighbor(n);
        if (!nowD) Config.addUnique(newNeighs, id);
      }
      if (nowD) {
        updated = n.update(pd, now, saveAll ? null : conts);
        // waste of time/mem, but descriptors are zipped,
        // thus checking before is expensive
        if (newBuddy) {
          if (!updated) { // new neighbor with no interesting content
            globalNeighborhood.removeNeighbor(id);
            erased = true;
            continue;
          } else {
            Config.addUnique(newNeighs, id);
          }
        }
        if (updated) {
          myContents.addNeighbor(n);
        }
      } else if (!erased) { // if goT && (!goD || c > 0)
        cid = t.getContentID();
        if (cd == null || !cd.ID.equals(cid)) {
          cd = myContents.getContent(cid);
        }
        if (cd == null) {
          System.err.println("Peer: ERROR : transaction received for inexistent content (ID = "
              + cid + ")!");
          System.err.flush();
          continue;
        }
        Config.addUnique(updates, cid);
        n.update(cd, t, now);
        cd.update(t, n);
        updated = true;
      }
    }

    // compute feedback for each modified content and new intentions for each
    // modified neighbor
    Iterator<String> sit = updates.iterator();
    while (sit.hasNext()) {
      cd = myContents.getContent(sit.next());
      cd.updateFeedback();
      // TODO: get new pref values for this content from ui, if any.
      cd.computeIntentions(); // TODO: use the local peer's prefs to compute
                              // intentions.
      ui.setFeedback(cd.ID, PAS_A, cd.pasAdequation);
      ui.setFeedback(cd.ID, PAS_S, cd.pasSatisfaction);
      ui.setFeedback(cd.ID, PAS_E, cd.pasSysEval);
      ui.setFeedback(cd.ID, PAC_A, cd.pacAdequation);
      ui.setFeedback(cd.ID, PAC_S, cd.pacSatisfaction);
      ui.setFeedback(cd.ID, PAC_E, cd.pacSysEval);
    }
    buildGlobalRanking();
    makeDescriptor();
  }  
  //actuTime = System.currentTimeMillis();
  if (myDescriptor.isValid()) {
    for (i = 0; i < newNeighs.size(); i++) {
      this.epidemic.sendCard(myDescriptor, newNeighs.get(i));
    }
  } else if (newNeighs.size() > 0) {
/**/System.err.println("Peer: ATTENTION: local peer's card not sent due to previous errors.");
    System.err.flush();
  }
  strLog = "}&[";
  String[] contentIds = myContents.getIDs();
  LocalContentData actCont = null;
  // Adding to log information about contents and WUW memory usage
  for(int k = 0; k < contentIds.length; k++){
    actCont = myContents.getContent(contentIds[k]);
    if( k == contentIds.length - 1 )
      strLog += "{'contentId': '" + contentIds[k] + "', " + actCont.getLogString() + "}";
    else{
      strLog += "{'contentId': '" + contentIds[k] + "', " + actCont.getLogString() + "}, ";
    }
  }
  strLog += "]&";
  strLog += Runtime.getRuntime().totalMemory() + "&";
  Config.logger.updateLogLine(strLog);
  Config.logger.writeCurrentLogLine();
  Config.logger.resetLogLine();
  System.out.println("My Contents :\n" + Config.printArray(myContents.toArray()));
  System.out.println("Current Neighborhood :\n" + Config.printArray(
      globalNeighborhood.toArray()));
}

/**
 * 
 * @return Implementation object of {@code PIHandler} interface
 * @author carvajal-r
 */
public PIHandler getPi() {
  return pi;
}


/************************ LOGGING FACILITIES ******************/

public Object[] getNeighborhood() {
  Object[] res;
  res = globalNeighborhood.toArray();
  return res;
}


public Object[] getContents() {
  Object[] res;
  res = myContents.toArray();
  return res;
}

}
