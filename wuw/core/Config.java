package wuw.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Random;

import wuw.comm.CommHandler;
import wuw.comm.TCPProtocol;
import wuw.comm.UDPProtocol;
import wuw.comm.newscast.Newscast;
import wuw.pi.PIHandler;
import wuw.pi.PIStub;
import wuw.ui.UIHandler;
import wuw.ui.WebUIHandler;


/**
 * Fully static class that contains all the information globally useful during
 * the execution of WUW. Its methods handle the initialization of WUW
 * components. It can be accessed anytime by any component.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 27
 */
public class Config {

public static final boolean printLogs = false;

static final String usageString = "For a correct execution, the following arguments are needed, in the given order :\n"
    + "    -- The IPv4 address to be used by this instance of WUW.\n"
    + "    -- The port number to be used by this instance of WUW.\n"
    + "    -- The transport protocol to be used (either \"TCP\" or \"UDP\").\n"
    + "    -- The file that contains the list of neighbors for this instance of WUW.\n";

/**
 * Random number generator to be used by every object during an execution.
 */
static public Random rand = null;

/**
 * This object represent the local peer.
 */
static private Peer localPeer = null;


/**
 * @return The local peer.
 */
static public Peer getLocalPeer() {
  return localPeer;
}


/**
 * This method must be called exactly once to initialize WUW. Any further
 * attempt to call this method will only generate an error message, without
 * modifying the existing configuration.
 * 
 * @param args
 *          Command line arguments
 * @return <code>true</code> if no error occurred while setting up the
 *         configuration, <code>false</code> otherwise.
 */
static public boolean set(String[] args) {

  if (localPeer != null) {
    System.err.println("CONFIG ERROR : config setting was attempted twice.");
    return false;
  }

  if (args.length < 4 // Just to catch some macroscopic mistyping...
      || !args[1].matches("[0-9]{4,5}")
      || !args[0].matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}")
      || !(args[2].equalsIgnoreCase("TCP") || args[2].equalsIgnoreCase("UDP"))) {
    usage();
    return false;
  }

  rand = new Random(System.currentTimeMillis());
  int localPort = Integer.valueOf(args[1]);
  PeerID pid = new PeerID(args[0], localPort);
  CommHandler com = args[2].equalsIgnoreCase("TCP") ? new TCPProtocol(pid) : new UDPProtocol(pid);
  Newscast news = new Newscast(null); // TODO: get parameters to newscast!
  UIHandler ui = new WebUIHandler(); // TODO: write a decent configuration....
  PIHandler pi = new PIStub(); // TODO: write a decent pi configuration....

  localPeer = new Peer(pid, com, ui, pi, news);
  pi.getPeers(readPeerList(args[args.length - 1], false)); // FIXME: to avoid crash on testing

  return true;
}


/**
 * Very simple method that reads from a well-formatted file and builds a list of
 * PeerIDs. It does not check for inconsistencies in the list, nor for duplicate
 * entries. If called after {@link Config#set(String[])} has been called, can
 * compare the PeerIDs in the provided list against the ID of the local peer,
 * not to include it in the output.
 * 
 * @param filePath
 *          The full path and file name where the list of peer must be read
 *          from.
 * @param shuffle
 *          If true, the entries in the resulting array will be shuffled.
 * @return An (possibly shuffled) array of PeerID read from the input file, or
 *         <code>null</code> if some exception occurs.
 */
static public PeerID[] readPeerList(String filePath, boolean shuffle) {
  LinkedList<PeerID> pList = new LinkedList<PeerID>();
  BufferedReader buf;
  String line;
  String[] tokens;
  PeerID p;
  try {
    buf = new BufferedReader(new FileReader(filePath));
    line = buf.readLine();
    while (line != null) {
      if (line.isEmpty()) continue;
      tokens = line.split("[\\s]+");
      if (tokens[0].startsWith("peer.", 0)) {
        p = new PeerID(tokens[1], Integer.valueOf(tokens[2]));
        if (localPeer == null || !p.equals(localPeer.localID)) {
          pList.add(p);
        }
      }
      line = buf.readLine();
    }
  }
  catch (Exception e) {
    System.err.println("Configuration : error while reading peer list input file.");
    e.printStackTrace();
    return null;
  }

  PeerID[] peerList = new PeerID[pList.size()];
  peerList = pList.toArray(peerList);
/**/if (printLogs) System.out.println("Peers : " + printArray(peerList));
  if (shuffle == true) peerList = shuffle(peerList, peerList.length);

  return peerList;
}


static private PeerID[] shuffle(PeerID[] orig, int size) {
  PeerID[] res = new PeerID[size];
  int[] temp = new int[orig.length];
  int ran;
  for (int i = 0; i < size;) {
    ran = Config.rand.nextInt(orig.length);
    if (temp[ran] == 0) {
      temp[ran] = 1;
      res[i] = orig[ran];
      i++;
    }
  }
  return res;
}


static private void usage() {
  System.err.println(usageString);
  return;
}


/**************************** DEBUG UTILITIES ********************************/

static public String printArray(Object[] a) {
  String res = "";
  if (a == null) {
    res = "\nNULL!\n";
  } else {
    for (int i = 0; i < a.length; i++) {
      res += "[" + i + "] " + a[i].toString() + "\n";
    }
  }
  return res;
}

}
