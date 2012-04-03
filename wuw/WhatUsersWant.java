

package wuw;


import wuw.core.Config;
import wuw.core.ContentData;
import wuw.core.ContentData.Category;
import wuw.core.ContentData.Interest;
import wuw.core.PeerID;


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
 * @throws InterruptedException
 */
public static void main(String[] args) throws InterruptedException {


  System.setProperty("java.net.preferIPv4Stack", "true");
  boolean go = Config.set(args);
  if (!go) {
    System.exit(1);
  }

  long start = System.currentTimeMillis();
  int duration = 20 * 1000;
  // TestTMsg.transportTest(args[args.length - 1]);
  TestOverlayConnection(args[args.length - 1]);
  while ((System.currentTimeMillis() - start) < duration) {
    try {
      Thread.sleep(duration);
    }
    catch (InterruptedException e) {
      System.err.println("Main : unexpected interruption while waiting for task completion...");
      e.printStackTrace();
      System.err.println("Main : going back to sleep...");
    }
  }
/**/// if (Config.printLogs) {
  System.out.println("My contents :\n" + Config.printArray(Config.getLocalPeer().getContents()));
  System.out.println("\nCurrent Neighborhood :\n"
      + Config.printArray(Config.getLocalPeer().getNeighborhood()));
  // }
  System.out.println("WUW execution happily ends here.");
  System.exit(0);
}


/**************************** DEBUG UTILITIES ********************************/

static public void TestOverlayConnection(String path) throws InterruptedException {
  PeerID[] neighs = Config.readPeerList(path, true);
  for (int i = 0; i < 20; i++) {
    Config.getLocalPeer().addContent("Content" + i, 10, Category.MOVIES, Interest.PRIMARY, neighs);
    Thread.sleep(10);
  }
}

}
