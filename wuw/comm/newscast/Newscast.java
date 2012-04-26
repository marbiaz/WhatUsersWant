

package wuw.comm.newscast;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.Timer;

import wuw.core.Config;
import wuw.core.PeerID;
import wuw.comm.CommHandler;
import wuw.comm.MsgHandler;


/**
 * This implementation of the NEWSCAST epidemic protocol provides the local peer
 * with an updated random sample of the whole set of known peers. It is
 * implemented as an independent protocol that runs on the local peer with a
 * fixed time period.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 13
 */
public class Newscast implements MsgHandler {

private boolean printLogs;

/**
 * The transport layer protocol used by the local peer.
 */
private CommHandler tProt;

/**
 * Milliseconds between two subsequent send events.
 */
private int delta;

private int maxCacheSize;

private int fanOut;

private int sendReply;

private int cacheSize;

private NCCacheEntry[] cache = null;

private final Object cacheLock;

private PeerID localNodeID = null;

private Timer sender = null;

private int mid;


/**
 * The protocol instance is created by the peer initialization routine (see
 * {@link Config#set(String[])}).
 * 
 * @param args
 */
public Newscast(int[] args) {// TODO: decent config!

  maxCacheSize = 50; //args[0];
  delta = 500; //args[1] * 1000;
  fanOut = 1; //args[2];
  sendReply = maxCacheSize + 1; //args[3];
  cacheLock = new Object();
  mid = -1;
  printLogs = Config.printLogs;

  int i = 0;
//  if (neighborList != null) {
//    int currentSize = (neighborList.length < maxCacheSize) ? neighborList.length
//        : maxCacheSize;
//    cache = new NCCacheEntry[currentSize];
//    for (; i < neighborList.length && i < currentSize; i++) {
//      cache[i] = new NCCacheEntry(neighborList[i].getPeerID());
//    }
//  } else {
    cache = new NCCacheEntry[0];
//  }
  cacheSize = i;

  sender = new Timer(delta, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
        send(getPeers(fanOut), false);
    }
  });
  sender.start();

}


void setDelta(int delay) {
  sender.setDelay(delay);
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.core.MsgHandler#handleMsg(java.lang.Object)
 */
public void handleMsg(Object msg) {

  NCMessage nMsg = (NCMessage)msg;
  NCCacheEntry[] reCache = nMsg.getCache();

  if ((reCache.length < sendReply) && !nMsg.isReply()) {
    send(new PeerID[] { reCache[0].getPeerID() }, true);
  }

  Object[] des;
  synchronized (cacheLock) {
    LinkedList<Integer> newEntries = mergeCaches(reCache);

    des = new Object[newEntries.size()];
    for (int i = 0; i < des.length; i++) {
      des[i] = cache[newEntries.poll()].getDescriptor();
    }

    if (printLogs) {
/**/ String log = "NEWSCAST: currently " + cacheSize + " descriptors :\n";
      for (int i = 0; i < cacheSize; i++) {
        log += cache[i].getPeerID().toString() + " - ";
      }
      log += "\n among which there are " + des.length + " new entries";
      System.out.println(log);
      System.out.flush();
    }
  }

  if (des.length > 0) {
    Config.getLocalPeer().getEpidemicUpdates(des);
  }

}


/**
 * It sends to a remote peer the descriptor of the local peer only.
 * Used whenever a new PeerID is added to the globalNeighborhood.
 * 
 * @param dest The remote peer which the descriptor is to be sent to.
 */
public void sendCard(Object descriptor, PeerID dest) {
  if (mid < 0) { // TODO: check : this block should be executed once by one thread only!
    localNodeID = Config.getLocalPeer().getPeerID();
    tProt = Config.getLocalPeer().getTransport();
    if (mid < 0) {
      mid = tProt.addMsgHandler(this);
    } else {
      System.err.println("Newscast : ERROR : addMsgHandler was attempted twice!!!");
      System.err.flush();
    }
  }
  if (dest == null) {
    System.err.println("Newscast : ERROR : null destination while sending local peer's card.");
    System.err.flush();
    return;
  }
  NCCacheEntry[] localCache = new NCCacheEntry[1];
  localCache[0] = new NCCacheEntry(localNodeID, descriptor);
  send(dest, localCache, false);
}


/*
 * It returns <peersNumber> PeerIDs chosen at random from this.cache. 
 */
private PeerID[] getPeers(int peersNumber) {
  PeerID res[];
  synchronized (cacheLock) {
    res = shuffle(cache, cacheSize, peersNumber);
  }
  return res;
}


/*
 * It returns the index of id in ids, or -1 if id is not in ids.
 * Synchronization on ids must be handled by the caller.
 */
private int contains(NCCacheEntry[] ids, NCCacheEntry id) {
  PeerID p = id.getPeerID();
  for (int i = 0; i < ids.length; i++) {
    if (ids[i] == null) break;
    if (ids[i].getPeerID().equals(p)) {
      return i;
    }
  }
  return -1;
}


/*
 * Returns a distinct shuffled array of PeerIDs.
 * To be called form within a 'synchronized (cache)' block.
 */
private PeerID[] shuffle(NCCacheEntry[] orig, int range, int size) {
  if (cacheSize == 0) return null;
  if (size > cacheSize) size = cacheSize;
  if (range > cacheSize) range = cacheSize;
  PeerID[] res = new PeerID[size];
  if (size == 1) { // just to speedup this case
    res[0] = cache[Config.rand.nextInt(range)].getPeerID();
  } else {
    int[] temp = new int[range];
    int ran;
    for (int i = 0; i < size;) {
      ran = Config.rand.nextInt(range);
      if (temp[ran] == 0) {
        temp[ran] = 1;
        res[i] = orig[ran].getPeerID();
        i++;
      }
    }
  }
  return res;
}


private void send (PeerID dest, NCCacheEntry[] localCache, boolean isReply) {
  NCMessage mess = new NCMessage(localCache, isReply);
  tProt.send(dest, mid, mess);
}


private void send(PeerID[] dest, boolean isReply) {
  if (mid < 0) {
    return;
  }
  NCCacheEntry[] localCache;
  Object descriptor;
  synchronized (cacheLock) {
    localCache = new NCCacheEntry[cacheSize + 1];
    if (cacheSize > 0) {
      for (int i = 0; i < cacheSize; i++) {
        cache[i].incTimestamp();
      }
      // TODO: (possibly harmful to info dissemination): for every content in target descriptor
      // choose from cache only the peers interested in that content. But only
      // once! Not easy... Now the same cache is sent to all the recipients....
      System.arraycopy(cache, 0, localCache, 1, cacheSize);
    }
  }
  if (dest != null) {
    descriptor = Config.getLocalPeer().getDescriptor();
    if (descriptor == null) return; // TODO: better to log that the message is discarded!
    localCache[0] = new NCCacheEntry(localNodeID, descriptor);
/**/if (printLogs && !isReply) {
      System.out.println("NEWSCAST: Timeout Fired, sending cache to " + Config.printArray(dest));
    }
    for (PeerID p : dest) {
      send(p, localCache, isReply);
    }
  } else if (printLogs) {
/**/System.out.println("NEWSCAST: Timeout Fired, but no peer is available!");
  }

}


private LinkedList<Integer> mergeCaches(NCCacheEntry[] reCache) {

  int recSize = reCache.length;
  int i = 0, localIndex = 0, recIndex = 0;
  LinkedList<Integer> newEntries = new LinkedList<Integer>();

/**/if (printLogs) {
    System.out.println("NEWSCAST: Merging my cache (" + cacheSize
        + " entries) with the received one (" + recSize + " entries).");
  }

  NCCacheEntry[] newCache = new NCCacheEntry[maxCacheSize];

  while ((i < maxCacheSize) && (localIndex < cacheSize) && (recIndex < recSize)) {
    boolean isFirst = false;

//    if (cache[localIndex].getTimestamp() == reCache[recIndex].getTimestamp()) {
//      isFirst = Config.rand.nextBoolean();
//    } else {
      isFirst = cache[localIndex].getTimestamp() <= reCache[recIndex].getTimestamp();
//    }

    if (isFirst) {
      if (contains(newCache, cache[localIndex]) < 0) {
        newCache[i] = cache[localIndex];
        i++;
      }
      localIndex++;
    } else {
      if (!localNodeID.equals(reCache[recIndex].getPeerID())
          && (contains(newCache, reCache[recIndex]) < 0)) {
        newCache[i] = reCache[recIndex];
        newEntries.add(i);
        i++;
      }
      recIndex++;
    }
  }

  // the local cache has more entries
  for (; (localIndex < cacheSize) && (i < newCache.length); localIndex++) {
    if ((contains(newCache, cache[localIndex]) < 0)) {
      newCache[i] = cache[localIndex];
      i++;
    }
  }
  // the received cache has more entries
  for (; (recIndex < recSize) && (i < newCache.length); recIndex++) {
    if ((!localNodeID.equals(reCache[recIndex].getPeerID()))
        && (contains(newCache, reCache[recIndex]) < 0)) {
      newCache[i] = reCache[recIndex];
      newEntries.add(i);
      i++;
    }
  }

  cache = newCache;
  cacheSize = i;

  return newEntries;
}

}
