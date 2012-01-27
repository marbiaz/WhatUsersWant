

package wuw.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;

import wuw.comm.CommHandler;
import wuw.comm.TCPProtocol;
import wuw.comm.UDPProtocol;


/**
 * Fully static class that contains all the information globally useful during
 * the execution of WUW. Its methods handle the initialization of WUW
 * components. It can be accessed anytime by any component.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 27
 */
public class Config {

static final String usageString = "For a correct execution, the following arguments are needed, in the given order :\n"
    + "    -- The IPv4 address to be used by this instance of WUW.\n"
    + "    -- The port number to be used by this instance of WUW.\n"
    + "    -- The transport protocol to be used (either \"TCP\" or \"UDP\").\n"
    + "    -- The file that contains the list of neighbors for this instance of WUW.\n";

/**
 * This object represent the local peer.
 */
static private Peer localPeer = null;


/**
 * The peerList of neighbors known to the local peer.
 */
static private PeerID[] peerList = null;


/**
 * @return The local peer.
 */
static public Peer getLocalPeer() {
  return localPeer;
}


/**
 * @return The peer list of neighbors.
 */
static public PeerID[] getPeerList() {
  return peerList;
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

  if (args.length < 4 // Just to catch some macroscopic mistyping...
      || !args[1].matches("[0-9]{4,5}")
      || !args[0].matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}")
      || !(args[2].equalsIgnoreCase("TCP") || args[2].equalsIgnoreCase("UDP"))) {
    usage();
    return false;
  }

  if (localPeer != null) {
    System.err.println("CONFIG ERROR : config setting was attempted twice.");
    return false;
  }

  int localPort = Integer.valueOf(args[1]);
  PeerID pid = new PeerID(args[0], localPort);
  CommHandler com = args[2].equalsIgnoreCase("TCP") ? new TCPProtocol(pid) : new UDPProtocol(pid);

  localPeer = new Peer(pid, com);
  peerList = readPeerList(args[3], localPeer.getPeerID());

  return true;
}


static private PeerID[] readPeerList(String filePath, PeerID localID) {
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
        if (!p.equals(localID)) {
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

  return peerList;
}


static private void usage() {
  System.err.println(usageString);
  return;
}

}
