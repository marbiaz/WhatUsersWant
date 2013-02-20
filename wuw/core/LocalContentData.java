/**
 * LocalContentData.java
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

package wuw.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import wuw.pi.Transaction;
import wuw.pi.Transaction.State;
import wuw.pi.Transaction.Type;
import wuw.core.PreferenceSet.PrefEntry;


/**
 * @author Marco Biazzini
 * @date 2012 Mar 15
 */
class LocalContentData extends ContentData {

ArrayList<Transaction> downloads; // local peer's ongoing incoming transactions
ArrayList<Transaction> uploads; // local peer's ongoing outgoing transactions
//ArrayList<Transaction> downloaded; // local peer's terminated incoming transactions
//ArrayList<Transaction> uploaded; // local peer's terminated outgoing transactions
double pasSatisfaction;
double pacSatisfaction;
double pasAdequation;
double pacAdequation;
double pasSysEval;
double pacSysEval;

private Neighbor latest;
private HashMap<Integer, Number[]> pacSatReminders;
private HashMap<Integer, Number[]> pasSatReminders;
private int pacUpdateCounter;
private int pasUpdateCounter;
private int downloadedItems;
private int uploadedItems;


LocalContentData(String id, int items, Map<String, String> preferences) {
  super(id, items, preferences);
  pasSatisfaction = Double.parseDouble(Config.getValue("localpeer", "pas_s")); // XXX: these are the default to express
  pacSatisfaction = Double.parseDouble(Config.getValue("localpeer", "pac_s")); // a 'neutral' judgment,
  pasAdequation = Double.parseDouble(Config.getValue("localpeer", "pas_a")); //   but attention should be payed
  pacAdequation = Double.parseDouble(Config.getValue("localpeer", "pac_a")); //   to how these values are updated...
  pasSysEval = Double.parseDouble(Config.getValue("localpeer", "pas_e"));
  pacSysEval = Double.parseDouble(Config.getValue("localpeer", "pac_e"));
  latest = null;
  downloadedItems = uploadedItems = 0;
  pasUpdateCounter = 0;
  pacUpdateCounter = 0;
  pacSatReminders = new HashMap<Integer, Number[]>(
      Integer.parseInt(Config.getValue("localpeer", "pacSatRem")));
  pasSatReminders = new HashMap<Integer, Number[]>(
      Integer.parseInt(Config.getValue("localpeer", "pasSatRem")));
}


private void addTransaction(Transaction t) {
  ArrayList<Transaction> list;
  if (t.getType() == Type.IN) {
    list = downloads;
  } else {
    list = uploads;
  }
  int pos = Collections.binarySearch(list, t); //, Transaction.altComparator
  if (pos < 0) {
    pos = -pos - 1;
  }
  list.add(pos, t);
}


void init(Neighbor[] n) {
  super.intentions = new ArrayList<Intention>();
  for (int j = 0; j < n.length; j++) {
    super.intentions.add(new Intention(n[j]));
  }
  Collections.sort(super.intentions);
  downloads = new ArrayList<Transaction>();
  uploads = new ArrayList<Transaction>();
//  downloaded = new ArrayList<Transaction>();
//  uploaded = new ArrayList<Transaction>();
}


void addNeighbor(Neighbor n) {
  Config.addUnique(intentions, new Intention(n));
}


void update(Transaction t, Neighbor n) {
  if (latest == null || !latest.equals(n)) {
    latest = n;
    addNeighbor(n);
  }
  addTransaction(t);
  if ((t.getType() == Type.IN) && (t.getState() == State.DONE)) {
    itemMap.set(t.getItem());
    version++;
  }
}


/*
 * The measures are computed on all the transactions received in the latest call to
 * pi.giveContentUpdates(). Then the lists of incoming and outgoing transactions
 * are cleared, thus no transaction is counted twice.
 * The Adequation is computed by averaging over the number of transactions
 * the average of the intentions towards all the peer that currently HAVE and item.
 * The Satisfaction requires some memory of the past computations.
 * Given that it is computed on completed items, the method store in the
 * 'pa{c|s}SatReminders' class fields the values computed for transactions that concern
 * incomplete items, in order to use these values the next time this method is called
 * and the same item is encountered.
 * With respect to SQLB formulas, here we consider the same intention value for all
 * the items belonging to the same content; thus the computation is greatly simplified.
 * Exponential running averages, based on a configurable parameter, are used to keep track
 * of the evolving measures.
 */
void updateFeedback() {
  Transaction t;
  PeerID n;
  double A = 0.0, S = 0.0, s = 0.0, v;
  int i, tItem = -1, currItem, currIndex = 0, countWrong = 0;
  Number[] nums; Intention intent;
  ArrayList<Integer> iTracker = new ArrayList<Integer>(); // completed items
  if (downloads.size() > 0) { // compute pac feedback
    pacUpdateCounter++;
    int[] counter = new int[items]; // for each item, how many seeders
    double[] sints = new double[items]; // for each item, sum of intentions toward seeders
    Arrays.fill(counter, 0);
    Arrays.fill(sints, 0.0);
    Iterator<Intention> iit = intentions.iterator();
    BitSet ncdItems;
    while (iit.hasNext()) {
      intent = iit.next();
      ncdItems = intent.remote.getContent(ID).contentInfo.itemMap;
      for (i = ncdItems.nextSetBit(0); i >= 0; i = ncdItems.nextSetBit(i+1)) {
        counter[i]++;
        sints[i] += intent.pacIntent;
      }
    }

    currItem = downloads.get(0).getItem();
    n = downloads.get(0).getRemote();
    intent = intentions.get(Collections.binarySearch(intentions, n));
    v = intent.pacIntent;
    for (i = 0; i <= downloads.size(); i++) {
      t = i == downloads.size() ? null : downloads.get(i);
      if (t == null || (tItem = t.getItem()) != currItem) {
        if (pacSatReminders.containsKey(currItem)) {
          nums = pacSatReminders.get(currItem);
        } else {
          nums = new Number[3];
          nums[0] = new Double(0.0); // intentions related to an item
          nums[1] = new Integer(0); // number of transaction related to an item
          nums[2] = new Integer(0); // number of wrong transaction related to an item
          pacSatReminders.put(currItem, nums);
        }
        nums[0] = nums[0].doubleValue() + s;
        nums[1] = nums[1].intValue() + i - currIndex;
        nums[2] = nums[2].intValue() + countWrong;
        if (((Integer)nums[1]).intValue() == 0) {
/**/      System.err.println("Peer : ERROR : Satisfaction counter became 0 while conputing content "
              + ID + " item " + currItem);
          System.err.flush();
        }
        if (((Integer)nums[2]).intValue() > ((Integer)nums[1]).intValue()) {
/**/      System.err.println("Peer : ERROR : inconsistent Satisfaction counter for content "
              + ID + " item " + currItem);
          System.err.flush();
        }
        if (((Double)nums[0]).isNaN()) {
/**/      System.err.println("Peer : ERROR : Satisfaction became NaN while conputing content "
              + ID + " item " + currItem);
          System.err.flush();
        }
        if (t == null) break;
        currItem = tItem;
        currIndex = i;
        s = 0.0;
        countWrong = 0;
      }
      if (!n.equals(t.getRemote())) {
        n = t.getRemote();
        intent = intentions.get(Collections.binarySearch(intentions, n));
        v = intent.pacIntent;
      }
      if (t.getState() != State.WRONG) {
        s += v;
        if (t.getState() == State.DONE) {
          Config.addUnique(iTracker, tItem);
        }
      } else
        countWrong++;
      if (counter[tItem] == 0) {
/**/    System.err.println("Peer : ERROR : Adequation counter for content "
              + ID + " item " + tItem + " is 0 !!!");
        System.err.flush();
      }
      A += (sints[tItem] + counter[tItem]) / (2 * counter[tItem]);
    }
    A = A / downloads.size();
    pacAdequation = (pacUpdateCounter < (1 / Peer.alpha)) ?
        ((pacAdequation * (pacUpdateCounter - 1)) + A) / pacUpdateCounter
        : ((1 - Peer.alpha) * pacAdequation) + (Peer.alpha * A);
    if (iTracker.size() > 0) {
      for (i = 0; i < iTracker.size(); i++) {
        nums = pacSatReminders.remove(iTracker.get(i));
        S += (nums[0].doubleValue() + nums[1].intValue() - nums[2].intValue())
            / (2.0 * nums[1].intValue());
      }
      S = S / (iTracker.size() * 1.0);
      pacSatisfaction = (pacUpdateCounter < (1 / Peer.alpha)) ?
          ((pacSatisfaction * (pacUpdateCounter - 1)) + S) / pacUpdateCounter
          : ((1 - Peer.alpha) * pacSatisfaction) + (Peer.alpha * S);
    }
    downloadedItems += iTracker.size();
    if (pacAdequation != 0)
      pacSysEval = pacSatisfaction / pacAdequation;
    else
      pacSysEval = 0.0;
    iTracker.clear();
    downloads.clear();
    // TODO: add current pac feedback to a cumulative running average...
  }

  A = 0.0; S = 0.0; s = 0.0; currIndex = 0; countWrong = 0;
  if (uploads.size() > 0) { // compute pas feedback
    pasUpdateCounter++;
    currItem = uploads.get(0).getItem();
    n = uploads.get(0).getRemote();
    intent = intentions.get(Collections.binarySearch(intentions, n));
    v = intent.pasIntent;
    for (i = 0; i <= uploads.size(); i++) {
      t = i == uploads.size() ? null : uploads.get(i);
      if (t == null || (tItem = t.getItem()) != currItem) {
        if (pasSatReminders.containsKey(currItem)) {
          nums = pasSatReminders.get(currItem);
        } else {
          nums = new Number[3];
          nums[0] = new Double(0.0);
          nums[1] = new Integer(0);
          nums[2] = new Integer(0);
          pasSatReminders.put(currItem, nums);
        }
        nums[0] = nums[0].doubleValue() + s;
        nums[1] = nums[1].intValue() + i - currIndex;
        nums[2] = nums[2].intValue() + countWrong;
        if (((Integer)nums[1]).intValue() == 0) {
/**/      System.err.println("Peer : ERROR : Satisfaction counter became 0 while conputing content "
              + ID + " item " + currItem);
          System.err.flush();
        }
        if (((Integer)nums[2]).intValue() > ((Integer)nums[1]).intValue()) {
/**/      System.err.println("Peer : ERROR : inconsistent Satisfaction counter for content "
              + ID + " item " + currItem);
          System.err.flush();
        }
        if (((Double)nums[0]).isNaN()) {
/**/      System.err.println("Peer : ERROR : Satisfaction became NaN while conputing content "
              + ID + " item " + currItem);
          System.err.flush();
        }
        if (t == null) break;
        currItem = tItem;
        currIndex = i;
        s = 0.0;
        countWrong = 0;
      }
      if (!n.equals(t.getRemote())) {
        n = t.getRemote();
        intent = intentions.get(Collections.binarySearch(intentions, n));
        v = intent.pasIntent;
      }
      if (t.getState() != State.WRONG) {
        s += v;
        if (new Double(v).isNaN()) {
/**/      System.err.println("Peer : ERROR : pas intention for neighbor "
              + n.toString() + " is NaN !!!");
          System.err.flush();
        }
        if (t.getState() == State.DONE) {
          Config.addUnique(iTracker, tItem);
          uploadedItems++;
        }
      } else {
        countWrong++;
      } 
      A += v;
    }
    A = (A + uploads.size() * 1.0) / (uploads.size() * 2.0);
    pasAdequation = (pasUpdateCounter < (1 / Peer.alpha)) ?
        ((pasAdequation * (pasUpdateCounter - 1)) + A) / pasUpdateCounter
        : ((1 - Peer.alpha) * pasAdequation) + (Peer.alpha * A);
    if (iTracker.size() > 0) {
      for (i = 0; i < iTracker.size(); i++) {
        nums = pasSatReminders.remove(iTracker.get(i)); 
        S += (nums[0].doubleValue() + nums[1].intValue() - nums[2].intValue())
            / (2.0 * (nums[1].intValue() - nums[2].intValue()));
      }
      S = S / iTracker.size();
      pasSatisfaction = (pasUpdateCounter < (1 / Peer.alpha)) ?
          ((pasSatisfaction * (pasUpdateCounter - 1)) + S) / pasUpdateCounter
          : ((1 - Peer.alpha) * pasSatisfaction) + (Peer.alpha * S);
    }
    if (pasAdequation != 0)
      pasSysEval = pasSatisfaction / pasAdequation;
    else
      pasSysEval = 0.0;
    // TODO: add current pas feedback to a cumulative running average...
    iTracker.clear();
    uploads.clear();
  }
}

/*
 * This way of compute intentions takes into account the cosine similarity
 * considering the exchange of pieces (upload/download)
 */
void computeIntentions() {
  String tmp1, strLog = "'intentions': {";
  Intention intent; Neighbor n;
  Transaction t; Iterator<Transaction> tit;
  NeighborContentData ncd; PreferenceSet neighPrefs;
  double pas, pac, pref;
  for (int i = 0; i < intentions.size(); i++) {
    pas = pac = pref = 0.0;
    intent = intentions.get(i);
    n = intent.remote;
    ncd = n.getContent(ID);
    neighPrefs = ncd.contentInfo.preferences;
    if (neighPrefs != null){
      pref = computeMappingFunction(neighPrefs);
    }
    if (ncd.downloads == null || ncd.downloads.size() == 0) {
      intent.pacIntent = pref;
    } else {
      tit = ncd.downloads.iterator();
      while (tit.hasNext()) {
        t = tit.next();
        if (t.getState() != State.WRONG) {
          pac++;
        }
      }
      pac = pac / ncd.downloads.size();
      ncd.downloads.clear();
      // pac intention : W*pref + (1 - W)*rep
      // change interval from [0..1] to [-1..1] : (X-A)/(B-A)*(D-C)+C
      pac = (pac * 2) - 1;
      intent.pacIntent = (Peer.pref_weight * pref)
          + ((1 - Peer.pref_weight) * pac);
    }
    if (ncd.uploads == null || ncd.uploads.size() == 0) {
      intent.pasIntent = pref;
    } else {
      tit = ncd.uploads.iterator();
      while (tit.hasNext()) {
        t = tit.next();
        if (t.getState() != State.WRONG) {
          pas++;
        }
      }
      pas = pas / ncd.uploads.size();
      ncd.uploads.clear();
      // change interval from [0..1] to [-1..1] : (X-A)/(B-A)*(D-C)+C
      pas = (pas * 2) - 1;
      intent.pasIntent = (Peer.pref_weight * pref)
          + ((1 - Peer.pref_weight) * pas);
    }
    if(i != intentions.size() - 1){
      tmp1 = n.ID != null ? "'" + n.ID.ip.getHostAddress() + "': {" : "'?': {";
      strLog += tmp1;
      strLog += "'pacInt': " + intent.pacIntent + ", 'pasInt': " + intent.pasIntent + "}, ";
    }else{
      tmp1 = n.ID != null ? "'" + n.ID.ip.getHostAddress() + "': {" : "'?': {";
      strLog += tmp1;
      strLog += "'pacInt': " + intent.pacIntent + ", 'pasInt': " + intent.pasIntent + "}";
    }
  }
  strLog += "}, ";
  Config.logger.updateLogLine(strLog);
}

/*
 * This way of compute intentions takes into account the cosine similarity
 * without considering the exchange of pieces (upload/download)
 */
//void computeIntentions() {
//  String tmp1, strLog = "'intentions': {";
//  Intention intent; Neighbor n;
//  NeighborContentData ncd; PreferenceSet neighPrefs;
//  for (int i = 0; i < intentions.size(); i++) {
//    intent = intentions.get(i);
//    n = intent.remote;
//    ncd = n.getContent(ID);
//    neighPrefs = ncd.contentInfo.preferences;
//    if(neighPrefs != null){
//      intent.pacIntent = computeMappingFunction(neighPrefs);
//      intent.pasIntent = -1;
//    }else{
//      intent.pacIntent = -1;
//      intent.pasIntent = -1;
//    }
//    if(i != intentions.size() - 1){
//      tmp1 = n.ID != null ? "'" + n.ID.ip.getHostAddress() + "': {" : "'?': {";
//      strLog += tmp1;
//      strLog += "'pacInt': " + intent.pacIntent + ", 'pasInt': " + intent.pasIntent + "}, ";
//    }else{
//      tmp1 = n.ID != null ? "'" + n.ID.ip.getHostAddress() + "': {" : "'?': {";
//      strLog += tmp1;
//      strLog += "'pacInt': " + intent.pacIntent + ", 'pasInt': " + intent.pasIntent + "}";
//    }
//  }
//  strLog += "}, ";
//  Config.logger.updateLogLine(strLog);
//}


public String toString() {
  String res = super.toString()
      + "\nFeedback has been computed " + pasUpdateCounter + " (pas) and "
      + pacUpdateCounter + " (pac) times so far."
      + "\nSatisfaction : pas = " + pasSatisfaction + "; pac = " + pacSatisfaction
      + "\nAdequation : pas = " + pasAdequation + "; pac = " + pacAdequation
      + "\nSysEval : pas = " + pasSysEval + "; pac = " + pacSysEval
      + "\nMy ongoing downloads: " + Config.printArray(downloads.toArray())
      + "\nMy ongoing uploads: " + Config.printArray(uploads.toArray())
//      + "\nMy terminated downloads: " + Config.printArray(downloaded.toArray())
//      + "\nMy terminated uploads: " + Config.printArray(uploaded.toArray())
      + "\n";
  return res;
}

/**
 * Getting a string of the current feedback separated by labels
 * @return Current feedback string representation
 * @author carvajal-r
 */
public String getLogString(){
	String res = "";
	res += "'pacAd': " + pacAdequation + ", 'pacSat': " + pacSatisfaction 
	      + ", 'pasAd': " + pasAdequation + ", 'pasSat': " + pasSatisfaction 
	      + ", 'pacSe': " + pacSysEval + ", 'pasSe': " + pasSysEval
	      + ", 'dowPi': " + downloadedItems + ", 'uplPi': " + uploadedItems;
	return res;
}

private double computeMappingFunction(PreferenceSet neighPrefs){
  PrefEntry[] localVecPref, neighVecPref;
  int numEqPrefs = 0;
  double intention = 0.0;
  String key;
  Iterator<String> keyIter = preferences.prefs.keySet().iterator();
  while(keyIter.hasNext()){
    key = keyIter.next();
    if(neighPrefs.prefs.containsKey(key)){
      System.out.println("Preference :: " + key);
      localVecPref = preferences.prefs.get(key);
      System.out.print("Local Vector :: ");
      for(int i = 0; i < localVecPref.length; i++)
        System.out.print(localVecPref[i].toString() + ", ");
      System.out.println();
      neighVecPref = neighPrefs.prefs.get(key);
      System.out.print("Neighbor's Vector :: ");
      for(int i = 0; i < neighVecPref.length; i++)
        System.out.print(neighVecPref[i].toString() + ", ");
      System.out.println();
      intention += getCosineSimilatiryMeasure(localVecPref, neighVecPref);
      System.out.println("IntentionSum :: " + intention);
      numEqPrefs++;
    }
  }
  if(intention == 0.0)
    return 0.0;
  return (intention) / (numEqPrefs * 1.0);
}

private double getCosineSimilatiryMeasure(PrefEntry[] localVecPref, PrefEntry[] neighVecPref){
  int i;
  double sum = 0.0, sumSqrLocal = 0.0, sumSqrNeigh = 0.0;
  String localPref, neighPref;
  for(i = 0; i < localVecPref.length; i++){
    localPref = localVecPref[i].getKey(); 
    for(int j = 0; j < neighVecPref.length; j ++){
      neighPref = neighVecPref[j].getKey();
      if(localPref.equalsIgnoreCase(neighPref)){
        sum += localVecPref[i].getValue() * neighVecPref[j].getValue();
        break;
      }
    }
  }
  if(sum == 0.0)
    return 0.0;
  for(i = 0; i < localVecPref.length; i ++)
    sumSqrLocal += Math.pow(localVecPref[i].getValue(), 2);
  for(i = 0; i < neighVecPref.length; i ++)
    sumSqrNeigh += Math.pow(neighVecPref[i].getValue(), 2);
  return (sum) / ( (Math.sqrt(sumSqrLocal)) * (Math.sqrt(sumSqrNeigh)));
}

}
