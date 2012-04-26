package wuw.pi.BT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class UnitTestBitTorrentStatistics {

/**
 * @param args
 * @throws IOException
 * @throws UnknownHostException
 */
public static void main(String[] args) throws UnknownHostException, IOException {
  String FromServer;
  Socket clientSocket = new Socket("localhost", 5000);
  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
      clientSocket.getInputStream()));
  FromServer = inFromServer.readLine();
  clientSocket.close();
  System.out.println(FromServer);
  BitTorrentStatistics bTstatistics = new BitTorrentStatistics(FromServer);

}

}
