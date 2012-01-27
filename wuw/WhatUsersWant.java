

package wuw;


import wuw.core.Config;


/**
 * This class executes a WUW instance on the local peer.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 16
 */
public class WhatUsersWant {

/**
 * Main function.
 * 
 * @param args
 *          Command line arguments for the execution.
 */
public static void main(String[] args) {


  System.setProperty("java.net.preferIPv4Stack", "true");
  boolean go = Config.set(args);
  if (!go) {
    System.exit(1);
  }

  TestTMsg.transportTest(Config.getLocalPeer(), Config.getPeerList());

  System.out.println("WUW execution happily ends here.");
  System.exit(0);
}


/**************************** DEBUG UTILITIES ********************************/

static private void arrayPrint(Object[] a) {
  String res = "";
  for (int i = 0; i < a.length; i++) {
    res += " " + a[i].toString();
  }
  res += "\n\n";
  System.out.println(res);
  return;
}

}
