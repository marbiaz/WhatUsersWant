/**
 * WhatUsersWant.java
 * 
 * Copyright 2012
 * This file is part of the WUW (What Users Want) service.
 * 
 * WUW is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or (at your option) any later version.
 * 
 * WUW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Foobar. 
 * If not, see http://www.gnu.org/licenses/.
 */

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
  int duration = Integer.parseInt(Config.getValue("config","duration"));
  // TestTMsg.transportTest(args[args.length - 1]);
  //TestOverlayConnection(args[args.length - 1]);

  PeerID[] neighs = Config.readPeerList(
      Config.getValue("config","globalPeerList"), true);
  Config.getLocalPeer().addContent(Config.getValue("content","id"), 
      Integer.parseInt(Config.getValue("content","pieces")), 
      Category.valueOf(Config.getValue("preferences","Category")), 
      Interest.valueOf(Config.getValue("preferences","Interest")) , neighs);
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
