package wuw.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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

public static boolean printLogs;

/*
static final String usageString = "For a correct execution, the following arguments are needed, in the given order :\n"
    + "    -- The IPv4 address to be used by this instance of WUW.\n"
    + "    -- The port number to be used by this instance of WUW.\n"
    + "    -- The transport protocol to be used (either \"TCP\" or \"UDP\").\n"
    + "    -- The file that contains the list of neighbors for this instance of WUW.\n";
 */
static final String usageString = "For a correct execution, the following arguments are needed:\n"
    + "    -- The XML file that contains all WUW parameters.\n"
    + "    -- The IPv4 address to be used by this instance of WUW.\n";

/**
 * Random number generator to be used by every object during an execution.
 */
static public Random rand = null;

/**
 * This object represent the local peer.
 */
static private Peer localPeer = null;

/**
 * Map representation of each item in XML configuration file
 * @author gomez-r
 */
@SuppressWarnings("rawtypes")
static private Map configParam;

/**
 * Useful for logging WUW statistics
 * @author gomez-r
 */
static public Logger logger;

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
  //TODO Change verification according to the new way of initialize WUW
  if ( args.length < 2 ) { // Just to catch some macroscopic mistyping...
    usage();
    return false;
  }
  //Setting configuration file parameters which are known till runtime phase
  if(!editConfigFile(args[0], args)){
    System.err.println("During the editing in XML file");
    return false;
  }
  //Load configuration values to an object in memory
  if(!loadConfig(args[0])){
    System.err.println("During the loading from XML file phase");
    return false;
  }
  printLogs = Boolean.parseBoolean(getValue("config","printLogs"));
  rand = new Random(System.currentTimeMillis());
  logger = Logger.getInstance(getValue("config","feedbackLogFile"));
  int localPort = Integer.parseInt(getValue("localpeer","portNumber"));
  PeerID pid = new PeerID(getValue("localpeer","ipAddress"), localPort);
  CommHandler com = getValue("localpeer","protocol").equalsIgnoreCase("TCP") ? 
      new TCPProtocol(pid) : new UDPProtocol(pid);
  Newscast news = new Newscast();
  UIHandler ui = new WebUIHandler(); // TODO: write a decent configuration....
  PIHandler pi = new wuw.pi.BT.BTHandler(getValue("config","globalPeerList"));
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
    while ((line = buf.readLine()) != null) {
      tokens = line.split("[\\s]+");
      if (tokens[0].startsWith("peer.", 0)) {
        p = new PeerID(tokens[1], Integer.valueOf(tokens[2]));
        if (localPeer == null || !p.equals(localPeer.localID))
          pList.add(p);
      }
    }
  }
  catch (Exception e) {
    System.err.println("Configuration : error while reading peer list input file.");
    e.printStackTrace();
    return null;
  }
  PeerID[] peerList = new PeerID[pList.size()];
  peerList = pList.toArray(peerList);
  if (printLogs) System.out.println("Peers : " + printArray(peerList));
  if (shuffle == true) peerList = shuffle(peerList, peerList.length);
  return peerList;
}

/**
 * Choose in a randomly way {@code size} items from {@code orig} array 
 * @param orig Source array
 * @param size Items in the new array
 * @return Randomly chosen array
 */
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

/**
 * Print to standard output how to lunch a WUW instance 
 */
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
 * Print an array representation of {@code a} to standard output
 * @param Array
 * @return String representation of the array
 * @author r-carvajal
 */
static public String printList(Object[] a) {
  String res = "[";
  if (a == null) {
    res = "[]";
  } else {
    for (int i = 0; i < a.length; i++) {
      if( i == a.length - 1)
        res += a[i].toString() + "]";
      else
        res += a[i].toString() + ", ";
    }
  }
  return res;
}

/**
 * Add new values to the tags in a XML file. {@code args} array must have the
 * values in the appropriate order
 * @param XML full path
 * @param Array of new values in the appropriate order
 * @return {@code True} if there were no errors during the loading, otherwise 
 * {@code False}
 * @author r-carvajal
 */
public static boolean editConfigFile(String xmlFile, String[] args){
  try {
    File configFile = new File(xmlFile);
    SAXBuilder builder = new SAXBuilder();
    Document conf = builder.build(configFile);
    Element root = conf.getRootElement();
    root.getChild("config").getChild("feedbackLogFile").setText(args[1]);
    root.getChild("config").getChild("globalPeerList").setText(args[2]);
    root.getChild("faketracker").getChild("context").setText(args[3]);
    root.getChild("faketracker").getChild("torrentsDir").setText(args[3] 
        + "/torrents");
    root.getChild("localpeer").getChild("ipAddress").setText(args[4]);
    root.getChild("localpeer").getChild("portNumber").setText(args[5]);
    root.getChild("faketracker").getChild("portNumber").setText(args[6]);
    root.getChild("bittorrent").getChild("bitTorrentPort").setText(args[7]);
    root.getChild("preferences").getChild("Interest").setText(args[8]);
    XMLOutputter xmlOutput = new XMLOutputter();
    xmlOutput.setFormat(Format.getPrettyFormat());
    xmlOutput.output(conf, new FileWriter(xmlFile));
    return true;
  }
  catch (JDOMException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  return false;
}

/**
 * Load WUW parameters from a XML file to {@code configParam} attribute
 *@param XML full path
 *@author r-carvajal
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
private static boolean loadConfig(String xmlFile) {
  File configFile = new File(xmlFile);
  SAXBuilder builder = new SAXBuilder();
  Iterator childIte, paramIte;
  String key, subKey, subItem;
  Map result = new HashMap();
  HashMap item;
  Element children, param;
  if (configFile.exists())
    try {
      Document conf = builder.build(configFile);
      Element root = conf.getRootElement();
      childIte = root.getChildren().iterator();
      while(childIte.hasNext()){
        children = (Element) childIte.next();
        key = children.getName();
        item = new HashMap();
        paramIte = children.getChildren().iterator();
        while(paramIte.hasNext()){
          param = (Element) paramIte.next();
          subKey = param.getName();
          subItem = param.getText();
          item.put(subKey, subItem);
        }
        result.put(key, item);
      }
      configParam = result;
      return true;
    } catch (Exception e) {
      System.err.println("Error while loading parameters");
    }
  else
    System.err.println("No XML configuration file found");
  return false;
}

/**
 * Get the text of the label {@code param} in children {@code children} from the 
 * XML file
 * @param children Children in XML file
 * @param param Label in the children
 * @return Text of the specified label
 * @author r-carvajal
 */
@SuppressWarnings("rawtypes")
public static String getValue(String children, String param){
  String strResult = null;
  Map childrenMap = (Map)configParam.get(children);
  if(childrenMap != null){
    strResult = (String) childrenMap.get(param);
    if(strResult == null)
      System.err.println("Error: item '" + param + "' does not exists " +
          "in XML configuration file");
  }
  else
    System.err.println("Error: item '" + children + "' does not exists " +
        "in XML configuration file");
  return strResult;
}

}