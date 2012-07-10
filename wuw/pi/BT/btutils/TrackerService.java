/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package wuw.pi.BT.btutils;

import java.io.*;
import java.net.*;
import java.util.*;
import simple.http.*;
import simple.http.load.*;
import simple.http.serve.*;
import wuw.core.Config;
import wuw.pi.BT.BtWuwPeer;

/**
 * Service called to answer a peer requesting information about peers sharing a 
 * given torrent
 * 
 * Modified by @author carvajal-r
 */
public class TrackerService extends Service {

public static enum AnnounceEvent {
STARTED, STOPPED, COMPLETED, EMPTY
};

/**
 * Default constructor for the tracker service, requested by the Simple server
 * @param context Context that was provided to the Simple server
 */
public TrackerService(Context context) {
  super(context);
}

/**
 * Method called when a request is received at the tracker address
 * It constructs the response message according to the parameters of the request.
 *
 * @param req Request message received from a peer with its parameters.
 * Parameters must be:
 * -- peer_id: the id of the peer that addresses the request
 * -- info_hash: the SHA1 hash of the torrent the peer is sharing and requesting
 * -- port: the port the peer is listening on
 * -- ip: (optional) the ip address of the peer
 * -- updated: total amount already uploaded by the peer
 * -- downloaded: total amount already downloaded by the peer
 * -- left: number of bytes left to complete download. O means peer is a seed

 * @param resp Response message that will be constructed and returned.
 * The response is a bencoded dictionary containing the list of all peers currently sharing the file given in parameter
 *
 * @throws IOException
 */ 

@SuppressWarnings({ "rawtypes", "unchecked" })
public void process(Request req, Response resp) throws IOException {
  Peer peer;
  HashMap hm = parseURI(req.getURI());
  AnnounceEvent event = null;
  byte[] answer = null;
  BtWuwPeer[] peers = null;
  ArrayList peerList = null;
  TreeMap ans = new TreeMap(), peerMap;
  if(hm != null){
    TreeMap param = new TreeMap(hm);
    if (param.get("ip") == null)
      param.put("ip", req.getInetAddress().toString().substring(1));
    event = getAnnounceType((String) param.get("event"));
    if( event.equals(AnnounceEvent.EMPTY) ){
      // Re-request message sent by the BT local instance
      // 1. Ask to WUW core the best ranked peers
      // 2. Sent them to the BT local instance
      if( param.get("compact") != null ){
        System.out.println("*** BEST RANKED PEERS ***");
        peers = Config.getLocalPeer().getPi().getBestRankedPeers();
      }else
        System.out.println("In Re-request Msg compact is null");
    }else{
      // &event= 'started' || 'completed' || 'stopped'
      // IF event.equals(AnnounceEvent.STARTED) for WUW could be useful
      // COMPLETED or STOPPED event, any message must be sent to BT
      // TODO: What is the action that WUW has to do? ending process; to see 
      // which content the BT client is related about; more ideas?
      // Request req, byte[]
      // Announce message received by the local BT instance
      // 1. Chose a set of peers in a randomly way. How many?
      // 2. Sent them to the local BT instance
      System.out.println("*** PEERS FROM FIRST ANNOUNCE ***");
      peers = Config.getLocalPeer().getPi().getPeersForAnnounce();
    }
    if(peers != null){
      peerList = new ArrayList(peers.length);
      for(int i = 0; i < peers.length; i++){
        peer = new Peer();
        peer.setIP(peers[i].getIpAddress());
        peer.setPort(peers[i].getBtPort());
        peerMap = new TreeMap();
        peerMap.put("ip", peer.getIP());
        peerMap.put("port", Integer.valueOf(peer.getPort()));
        peerList.add(peerMap);
        System.out.println(peer.getIP() + ":" + peer.getPort());
      }
    }
  }else{
    peerList = new ArrayList(0);
    ans.put("failure reason", "Malformed request...");
  }
  ans.put("peers", peerList);
  answer = BEncoder.encode(ans);
  resp.set("Content-Type", "text/plain");
  resp.setDate("Date", System.currentTimeMillis());
  resp.set("Server", Config.getValue("faketracker", "servername"));
  OutputStream out = resp.getOutputStream();
  out.write(answer);
  out.close();
}


/**
 * Method called to parse the Request URI and retrieve the parameters in it
 *
 * @param uri The URI to be parsed
 * @return A HashMap containing the parameters names (keys) and corresponding values
 * @throws UnsupportedEncodingException
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
private HashMap parseURI(String uri) throws UnsupportedEncodingException{
  String[] temp = uri.split("[?]");
  HashMap params = null;
  if(temp.length >= 2){
    String[] param = temp[1].split("[&]");
    params = new HashMap(param.length);
    for(int i = 0; i < param.length; i++){
      String[] splitParam = param[i].split("[=]");
      if(splitParam.length == 1)
        params.put(splitParam[0], "");
      else if(splitParam.length == 2)
        params.put(splitParam[0], URLDecoder.decode(splitParam[1],
            Constants.BYTE_ENCODING));
    }
    return params;
  }
  return null;
}

private AnnounceEvent getAnnounceType(String event){
  if(event == null)
    return AnnounceEvent.EMPTY;
  if(event.toUpperCase().equalsIgnoreCase(AnnounceEvent.COMPLETED.toString()))
    return AnnounceEvent.COMPLETED;
  if(event.toUpperCase().equalsIgnoreCase(AnnounceEvent.EMPTY.toString()))
    return AnnounceEvent.EMPTY;
  if(event.toUpperCase().equalsIgnoreCase(AnnounceEvent.STARTED.toString()))
    return AnnounceEvent.STARTED;
  if(event.toUpperCase().equalsIgnoreCase(AnnounceEvent.STOPPED.toString()))
    return AnnounceEvent.STOPPED;
  return null;
}

}
