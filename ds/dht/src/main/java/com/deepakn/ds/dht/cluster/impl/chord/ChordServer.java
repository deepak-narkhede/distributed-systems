package com.deepakn.ds.dht.cluster.impl.chord;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Chord server responsible for accepting the requests from client or forwarded by members.
 */
public class ChordServer
{

  // chord member in which server is running.
  private ChordNode owner;

  // information related to server socket on the owner node.
  private ServerSocket serverSocket;

  // information related to client connection.
  private Socket clientSocket;

  // server listens on this port.
  private int listenPort;

  // gracefully stop the server.
  private boolean stop;

  public ChordServer(ChordNode owner)
  {
    try {
      this.owner = owner;
      this.listenPort = owner.getListenPort();
      this.stop = false;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Runs the chord server on the specified port.
   */
  public void start()
  {
    System.out.println("Started chord server on" + this.owner.getIp() + ":" + this.owner.getListenPort());
    try {
      serverSocket = new ServerSocket(listenPort);
      while (!stop) {
        clientSocket = serverSocket.accept();
        System.out.println("Connected with : " + clientSocket.getInetAddress() + " at " + new Date());
        Thread service = new Thread(new ServerRequestHandler(owner, clientSocket));
        service.start();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      this.stop();
    }
  }

  /**
   * Stops the server.
   */
  public void stop(){
    try {
      this.stop = true;
      this.serverSocket.close();
    }
    catch (IOException e){
      e.printStackTrace();
    }
  }



}


