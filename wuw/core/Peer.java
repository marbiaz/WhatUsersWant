

package wuw.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Timer;

import wuw.comm.CommHandler;
import wuw.comm.newscast.Newscast;
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
final ArrayList<ContentData> contents;
final ArrayList<Neighbor> globalNeighborhood;
BlockingQueue<PeerDescriptor> epidemicUpdates;
// BlockingQueue<Transaction> p2pUpdates;
Timer computer;

public final boolean sharePrefs = true; // TODO: make these configurable!
public final boolean shareInts = true;

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
  contents = new ArrayList<ContentData>();
  globalNeighborhood = new ArrayList<Neighbor>();
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
  ContentData[] c = null;
  int i = 0;
  synchronized (contents) {
    c = new ContentData[contents.size()];
    Iterator<ContentData> cit = contents.iterator();
    while (cit.hasNext()) {
      c[i++] = cit.next();
    }
  }
  return new PeerDescriptor(localID, System.currentTimeMillis(), c);
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
 * @param id
 *          The ID of the content to get.
 * @return The {@link ContentData} identified by the given ID, or
 *         <code>null</code>, if no such content is found.
 */
public ContentData getContent(String id) {
  ContentData res = null;
  synchronized (contents) {
    int i = Collections.binarySearch(contents, new ContentData(id));
    if (i >= 0) {
      res = contents.get(i);
    }
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
public boolean addContent(ContentData c) {
  PeerID remote;
  int l = c.intentions.length;
  int chosen = Config.rand.nextInt(l); // send at least 1 msg for sure!
  synchronized (globalNeighborhood) { // to avoid releasing the lock (see
                                      // addNeighbor) after each iteration...
    for (int i = 0; i < l; i++) {
      remote = c.intentions[i].remote;
      addNeighbor(new Neighbor(remote, System.currentTimeMillis()),
          (i == chosen || Config.rand.nextDouble() < (Math.log(l) / l)));
      // TODO: add remote to the list to be given to P2P application!!!
    }
  }
  synchronized (contents) {
    int i = Collections.binarySearch(contents, c);
    if (i < 0) {
      contents.add(-i - 1, c);
    }
  }

  return true;
}


void addNeighbor(Neighbor n, boolean sendCard) {
  synchronized (globalNeighborhood) {
    int index = Collections.binarySearch(globalNeighborhood, n);
    if (index < 0) {
      globalNeighborhood.add(-index - 1, n);
    }
  }
  if (sendCard) {
    this.epidemic.sendCard(n.ID);
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
/**/if (printLogs)
    System.out.println("Peer : The epidemic updates queue currently contains "
        + epidemicUpdates.size() + " descriptors.");
}


private Neighbor nLookUp(PeerID p) {
  Neighbor n = null;
  int i = Collections.binarySearch(globalNeighborhood, new Neighbor(p));
  if (i >= 0) {
    n = globalNeighborhood.get(i);
  }
  return n;
}


private ContentData cLookUp(String c) {
  ContentData res = null;
  int i = Collections.binarySearch(contents, new ContentData(c));
  if (i >= 0) {
    res = contents.get(i);
  }
  return res;
}


private void doTheMagic() {
  // TODO: compute satisfaction and adequation and sysEvaluation.

  ArrayList<PeerDescriptor> newDescriptors = new ArrayList<PeerDescriptor>();
  epidemicUpdates.drainTo(newDescriptors);
  Collections.sort(newDescriptors);

  Transaction[] newTrans = pi.getContentUpdates();
  // TODO : what if no transaction/descriptors??
  Arrays.sort(newTrans);

  PeerID id;
  Neighbor n, m;
  PeerDescriptor pd;
  Transaction t;
  Iterator<PeerDescriptor> pdit = newDescriptors.iterator();
  HashMap<String, BitSet> cmap;
  String cid, pcid;
  BitSet newItems;
  synchronized (globalNeighborhood) {
    for (int i = 0; pdit.hasNext(); i++) {
      n = globalNeighborhood.size() <= i ? null : globalNeighborhood.get(i);
      pd = pdit.next();
      if (n == null || (n.ID.compareTo(pd.ID) > 0)) {
        // TODO: add new neighbor in position i
        // TODO: keep track of the neighbor
      } else if (n.ID.equals(pd.ID)) {
        // TODO: update n with pd's data
        // TODO: keep track of the neighbor
      }
    }

/**//* if (printLogs) */System.out.println("New transactions :\n" + Config.printArray(newTrans));
    newItems = new BitSet();
    pcid = newTrans[0].getContentID();
    m = null;
    cmap = new HashMap<String, BitSet>();
    for (int i = 0; i < newTrans.length; i++) {
      t = newTrans[i];
      id = t.getRemote();
      cid = t.getContentID();
      if (m != null && !id.equals(m.ID)) {
        // TODO: add it to neighborhood
        m = null;
      }
      n = nLookUp(id);
      if (m == null && n == null) {
        // TODO: create such a neighbor m
      } else if (m == null) {
        m = n;
      }
      // TODO: update m ...
      // TODO: keep track of the neighbor, but only ONCE!!
      if ((t.getType() == Type.IN) && (t.getState() == State.DONE)) {
        if (!pcid.equals(cid)) {
          if (!newItems.isEmpty()) {
            if (cmap.containsKey(pcid)) {
              cmap.get(pcid).or(newItems);
            } else {
              cmap.put(pcid, newItems);
            }
            newItems = new BitSet();
          }
          pcid = cid;
        }
        newItems.set(t.getItem());
      }
    }
  }

  ContentData cd;
  synchronized (contents) { // ATTENTION : from now on, no modification to the
                            // neighborhood!!!
    Iterator<String> ci = cmap.keySet().iterator();
    while (ci.hasNext()) {
      cid = ci.next();
      newItems = cmap.get(cid);
      cd = cLookUp(cid);
      if (cd == null) {
        System.err
            .println("Peer: UNRECOVERABLE ERROR : transaction received for inexistent content!");
        System.err.flush();
        System.exit(2); // FIXME: Think of something better...
      }
      cd.itemMap.or(newItems);
    }

    // TODO:  get new pref values from ui, if any.

    // TODO:  compute new intentions per peer per content.
  }

/**//*if (printLogs) */System.out.println("Contents :\n" + Config.printArray(contents.toArray()));

  // TODO:  send satisfaction and adequation and sysEvaluation to ui.

  // TODO:  compute new ranked list of neighbors and give it to pi

  // TODO:  optimizations:
  //        apply epidemic updates to global neighborhood (track who is modified)
  //        apply content updates to local contentData (track what is modified)
  //        compute novel intentions for the MODIFIED neighbors/contents
}

}
