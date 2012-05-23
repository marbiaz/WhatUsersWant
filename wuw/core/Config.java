package wuw.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

import wuw.comm.CommHandler;
import wuw.comm.TCPProtocol;
import wuw.comm.UDPProtocol;
import wuw.comm.newscast.Newscast;
import wuw.pi.PIHandler;
import wuw.ui.UIHandler;
import wuw.ui.WebUI.WebUIHandler;


/**
 * Fully static class that contains all the information globally useful during
 * the execution of WUW. Its methods handle the initialization of WUW
 * components. It can be accessed anytime by any component.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 27
 */
public class Config {

public static final boolean printLogs = true;

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
 * PVO
 * @author gomez-r
 */
@SuppressWarnings("rawtypes")
private static Map configParam;

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
  PIHandler pi = new wuw.pi.BT.BTHandler(readPeerList(args[args.length - 1], false)); //wuw.pi.PIStub();
  loadConfig(args[3]);
  localPeer = new Peer(pid, com, ui, pi, news);

  //pi.getPeers(null, readPeerList(args[args.length - 1], false)); // FIXME: to avoid crash on testing

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


/**************************** UTILITIES ********************************/

/**
 * It adds {@link java.lang.Comparable} objects (of any type) to the given list.
 * The list will be always ordered according to the natural ordering of the items.
 * No duplicates are allowed in the list, thus no addition occurs if an item is
 * already in the list.<br>
 * No type checking on the objects being added is performed. Thus the caller must
 * be sure that the items being added are consistent with respect to their
 * mutual comparison.
 *
 * @param set The list that hosts the items
 * @param item The oject to be added
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
static public void addUnique(List set, Comparable item) {
  int i = Collections.binarySearch(set, item);
  if (i < 0) {
    set.add(-i - 1, item);
  }
}


/**
 * It provides the printout of all the objects in the given array,
 * one per line, each line starting with the array index of the object.
 *
 * @param a array of objects to be printed
 * @return A String containing the objects printout.
 */
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

/**
 * A new proposal for parameterize WUW's values from a XML file. For short (Parameterize Version
 * One PVO) 
 * @author gomez-r
 */

/**
 * Load parameters from a XML file
 *@param config the path to the XML configuration file
 *@author gomez-r
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
private static void loadConfig(String config) {
    File configFile = new File(config);
    SAXBuilder sb = new SAXBuilder();
    HashMap params = new HashMap();
    if (configFile.exists())
        try {
            Document conf = sb.build(configFile);
            Element root = conf.getRootElement();
            for (Iterator it = root.getDescendants(new ElementFilter(
                    "param"));
                               it.hasNext(); ) {
                Element param = (Element) it.next();
                params.put(param.getAttribute("name").getValue(),
                           param.getAttribute("value").getValue());
            }
            configParam = params;
        } catch (Exception e) {
            System.err.println("Error while loading parameters");
        }
    else {
        System.err.println("No configuration file found...");
        System.exit(0);
    }
}

/**
 * 
 * @param param
 * @return
 * @author gomez-r
 */
public static Object get(String param) {
    return configParam.get(param);
}


}
