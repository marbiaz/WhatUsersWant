

package wuw;


import wuw.core.Config;
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
  System.setProperty("file.encoding", "UTF-8");
  boolean go = Config.set(args);
  if (!go) {
    System.exit(1);
  }

  long start = System.currentTimeMillis();
  int duration = 7200 * 1000;
  // TestTMsg.transportTest(args[args.length - 1]);
  //TestOverlayConnection(args[args.length - 1]);
  PeerID[] neighs = Config.readPeerList(args[args.length - 1], true);
  Config.getLocalPeer().addContent("eclipse.tar.gz", 429, Category.MOVIES, Interest.PRIMARY, neighs);
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
/**/ if (Config.printLogs) {
  System.out.println("My contents :\n" + Config.printArray(Config.getLocalPeer().getContents()));
  System.out.println("\nCurrent Neighborhood :\n"
      + Config.printArray(Config.getLocalPeer().getNeighborhood()));
   }
  System.out.println("WUW execution happily ends here.");
  System.exit(0);
}


/**************************** DEBUG UTILITIES ********************************/

static public void TestOverlayConnection(String path) throws InterruptedException {
  int first, f = 0, last, l = 0, middle;
  PeerID[] curr, neighs = Config.readPeerList(path, true);
  middle = neighs.length / 2;
  if (neighs.length % 2 == 0) {
    f--;
  } else {
    l++;
  }
  for (int i = 0; i < 20; i++) {
    first = f + middle - Config.rand.nextInt(middle);
    last = l + middle + Config.rand.nextInt(middle);
    curr = new PeerID[last - first + 1];
    System.arraycopy(neighs, first, curr, 0, curr.length);
    Config.getLocalPeer().addContent("Content" + i, 10, Category.MOVIES, Interest.PRIMARY, curr);
    Thread.sleep(10);
  }
  System.out.println(Config.printArray(Config.getLocalPeer().getContents()));
  System.out.println(Config.printArray(Config.getLocalPeer().getNeighborhood()));
}

}
