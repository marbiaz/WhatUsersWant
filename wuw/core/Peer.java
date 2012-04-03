

package wuw.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Timer;

import wuw.comm.CommHandler;
import wuw.comm.newscast.Newscast;
import wuw.core.ContentData.Category;
import wuw.core.ContentData.Interest;
import wuw.pi.Transaction;
import wuw.pi.PIHandler;
import wuw.pi.Transaction.State;
import wuw.pi.Transaction.Type;
import wuw.ui.UIHandler;


/**
 * An instance of this class will contain all that belongs to the local peer.
 * 
 * @author Marco Biazzini
 * @date 2012 January 20
 */
public class Peer {

private boolean printLogs;

final CommHandler transport;
final UIHandler ui;
final PIHandler pi;
final Newscast epidemic;
final PeerID localID;
final ContentBasket myContents;
final Neighborhood globalNeighborhood;
BlockingQueue<PeerDescriptor> epidemicUpdates;
Timer computer;

public final boolean sharePrefs = true; // TODO: make these 3 configurable!
public final boolean shareInts = true;
private final boolean saveAll = false; // -> not saving info about unknown contents
// TODO:  this info (this neighbor sharing with others a content local peer is
//        NOT currently trading) could reveal more about neighbor's interests.
//        Surely it boosts up memory requirements...

double pasSatisfaction = 0.5;
double pacSatisfaction = 0.5;
double pasAdequation = 0.5;
double pacAdequation = 0.5;
double pasSysEval = 1;
double pacSysEval = 1;


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
  this.ui = ui;
  this.pi = pi;
  myContents = new ContentBasket();
  globalNeighborhood = new Neighborhood();
  epidemicUpdates = new LinkedBlockingQueue<PeerDescriptor>(100);
  // p2pUpdates = new LinkedBlockingQueue<Transaction>(100);
  printLogs = Config.printLogs;

  computer = new Timer(2000, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      doTheMagic();
    }
  });
  computer.start();

}


public Object getDescriptor() {
  // if (!(sharePrefs || shareInts)) return me; // ... what about c.ID & Co. ?
  Object res = null;
  synchronized (myContents) {
  synchronized (globalNeighborhood) {
    if (myContents.size() > 0) {
      ContentData[] c = myContents.toArray();
      res = new PeerDescriptor(localID, c);
    }
  }}
  return res;
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
public boolean addContent(String id, int items, Category cat, Interest interest, PeerID[] peerList) {
  PeerID remote;
  Neighbor n;
  LocalContentData c;
  boolean res = false;
  int i, k = 0, l = peerList.length;
  int j = l - 1;
  long now = System.currentTimeMillis();
  Neighbor[] neighs = null;
  synchronized (myContents) {
  synchronized (globalNeighborhood) {
    c = myContents.getContent(id);
    if (c == null) {
      c = new LocalContentData(id, items, cat, interest);
      myContents.addContent(c);
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
      c.initIntentions(neighs);
      // FIXME: if a global param says that info about unknown content should
      // not be shared, the following should always be done on the peerList
      Object descriptor = Config.getLocalPeer().getDescriptor();
      if (descriptor != null) {
      int chosen = k > 0 ? Config.rand.nextInt(k) : 0; // send at least 1 msg
      for (i = 0; i < k; i++) {
        if (i == chosen || Config.rand.nextDouble() < (Math.log(k) / k)) {
          this.epidemic.sendCard(descriptor, neighs[i].ID);
        }
      }
      } else {
/**/    System.err.println("Peer: ATTENTION: local peer's card not sent due to previous errors.");
        System.err.flush();
      }
    }
  }}
  if (neighs != null) {
    // FIXME: c and peerList must be given to P2P application!!!
  }
  return res;
}


@SuppressWarnings({ "unchecked", "rawtypes" })
private void addUnique(List set, Comparable item) {
  int i = Collections.binarySearch(set, item);
  if (i < 0) {
    set.add(-i - 1, item);
  }
}


/**
 * Get the {@link PeerDescriptor}s updates from the overlay manager.
 * 
 * @param upd
 *          Descriptors to get
 */
public void getEpidemicUpdates(Object upd[]) {
  int i = 0;
  boolean ok, clog = false;
  while (i < upd.length) {
    ok = epidemicUpdates.offer((PeerDescriptor)upd[i]);
    if (ok)
      i++;
    else
      clog = true;
  }
  if (clog) {
/**/System.err.println("Peer: ATTENTION: Epidemic updates queue congestion!");
    System.err.flush();
  }
/**/if (printLogs) {
    System.out.println("Peer : The epidemic updates queue currently contains "
        + epidemicUpdates.size() + " descriptors.");
  }
}


// TODO:  optimizations (to be thought carefully w.r.t. intentions update pace):
//        apply epidemic updates to global neighborhood (track who is modified)
//        apply content updates to local contentData (track what is modified)
//        compute novel intentions for
//        -- the MODIFIED neighbors (newDescriptors U newNeighs)
//        -- the MODIFIED content
private void doTheMagic() {

  int c;
  String conts[] = null;
  ArrayList<PeerDescriptor> temp = new ArrayList<PeerDescriptor>(); //FIXME: awful
  ArrayList<PeerDescriptor> newDescriptors = new ArrayList<PeerDescriptor>();

  c = epidemicUpdates.drainTo(temp);
  for (int i = c - 1; i >= 0; i--) {
    addUnique(newDescriptors, temp.get(i));
  }

  Transaction[] newTrans = pi.giveContentUpdates();

  // TODO: consider also new prefs from user in the next line...
  if (newDescriptors.size() == 0 && newTrans == null) return;

  PeerID id;
  Neighbor n;
  PeerDescriptor pd;
  Transaction t;
  LocalContentData cd;
  HashMap<String, BitSet> cmap;
  String cid, pcid = null;
  BitSet newItems = null;
  long now = System.currentTimeMillis();
  Iterator<PeerDescriptor> pdit;
  ArrayList<PeerID> newNeighs = new ArrayList<PeerID>();
  //boolean updated;

  Arrays.sort(newTrans);
  pdit = newDescriptors.iterator();
  synchronized (myContents) {
  synchronized (globalNeighborhood) {
    while (pdit.hasNext()) {
      pd = pdit.next();
      id  = pd.getPeerID();
      n = globalNeighborhood.getNeighbor(id);
      if (n == null) {
        n = new Neighbor(id, now);
        globalNeighborhood.addNeighbor(n);
        addUnique(newNeighs, id); // FIXME: not needed: newDescriptor is already ordered
      }
      /*updated =*/ n.update(pd, now, saveAll ? null : conts);
      //if (updated) {
        // TODO: keep track of 'n', but only once!
      //}
    }
/**/if (printLogs) System.out.println("Current Neighborhood :\n"
        + Config.printArray(globalNeighborhood.toArray()));

/**/if (printLogs) System.out.println("New transactions :\n" + Config.printArray(newTrans));
    cmap = new HashMap<String, BitSet>(); // it records newly downloaded pieces, if any
    if (newTrans != null) {
      n = null;
      for (int i = 0; i < newTrans.length; i++) {
        t = newTrans[i];
        cid = t.getContentID();
        id = t.getRemote();
        if (n == null || !id.equals(n.ID)) {
          n = globalNeighborhood.getNeighbor(id);
        }
        if (n == null) { // no such a neighbor in the neighborhood
          n = new Neighbor(id);
          globalNeighborhood.addNeighbor(n);
          addUnique(newNeighs, id);
        }
        n.update(t, now);
        // TODO: keep track of the neighbor, but only ONCE!!
        // update local peer's bitmap if needed
        if ((t.getType() == Type.IN) && (t.getState() == State.DONE)) {
          if (pcid == null || !pcid.equals(cid)) {
            if (cmap.containsKey(cid)) {
              newItems = cmap.get(cid);
            } else {
              newItems = new BitSet();
              cmap.put(cid, newItems);
            }
            pcid = cid;
          }
          newItems.set(t.getItem());
        }
      }
    }
    // TODO: find all neighbors having each item of the latest k transactions....

    Iterator<String> ci = cmap.keySet().iterator();
    while (ci.hasNext()) {
      cid = ci.next();
      newItems = cmap.get(cid);
//      for (int i = 0; i < newTrans.length; i++) {
//        t = newTrans[i];
//        cid = t.getContentID();
      cd = myContents.getContent(cid);
      if (cd == null) {
        System.err
            .println("Peer: ERROR : transaction received for inexistent content (ID = "
            + cid + ")!");
        System.err.flush();
      } else {
        //TODO: update content cd
        cd.itemMap.or(newItems);
        cd.version++;
      }
    }
    // TODO: compute satisfaction and adequation and sysEvaluation.
    // TODO:  get new pref values from ui, if any.
    // TODO:  compute new intentions per peer per content. Store the values, to be used later on.
    // TODO: save the newly computed intentions to each content ...

    Object descriptor = Config.getLocalPeer().getDescriptor();
    if (descriptor != null) {
      for (int i = 0; i < newNeighs.size(); i++) {
        this.epidemic.sendCard(descriptor, newNeighs.get(i));
      }
    } else {
/**/  System.err.println("Peer: ATTENTION: local peer's card not sent due to previous errors.");
      System.err.flush();
    }
  }}
//
/**/if (printLogs) System.out.println("My Contents :\n" + Config.printArray(myContents.toArray()));

  // TODO: compute new ranked list of neighbors and give it to pi
  // TODO: send satisfaction and adequation and sysEvaluation to ui.
}


// FIXME: just to ease the testing....
/************************ TEST FACILITIES ******************/
public Object[] getNeighborhood() {
  Object[] res;
  synchronized (globalNeighborhood) {
    res = globalNeighborhood.toArray();
  }
  return res;
}


public Object[] getContents() {
  Object[] res;
  synchronized (myContents) {
    res = myContents.toArray();
  }
  return res;
}

}
