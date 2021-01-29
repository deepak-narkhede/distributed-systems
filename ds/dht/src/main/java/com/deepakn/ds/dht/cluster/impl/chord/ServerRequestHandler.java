package com.deepakn.ds.dht.cluster.impl.chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.deepakn.ds.dht.common.ClientRequest;
import com.deepakn.ds.dht.common.ServerResponse;
import com.deepakn.ds.dht.cluster.ClusterDataOps;
import com.deepakn.ds.dht.cluster.RemoteMethodsForCommWithinRing;
import com.deepakn.ds.dht.cluster.utils.Key;

/**
 * This class represents request handler which handles all requests send to specific chord member.
 * Based on the request data availability on the chord member the appropriate response is returned or else
 * it forwards the request within the ring as per the chord protocol until it reaches the exact chord member.
 */
public class ServerRequestHandler implements Runnable {

  // chord member within the ring which manages request handler.
  private ChordNode owner;

  // information about the client connection.
  private Socket connection;

  // information about the client request.
  private ClientRequest request;

  public ServerRequestHandler(ChordNode owner, Socket connection)
  {
    this.owner = owner;
    this.connection = connection;
  }

  /**
   * main thread to process and handle the request on the chord member.
   */
  @Override
  public void run()
  {
    try {
      ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
      this.request = (ClientRequest)ois.readObject();
      if (this.owner.getNodeKey().compareTo(this.owner.getPredecessor().getNodeKey()) == 1) {
        if (request.getDataHashKey().isBetween(this.owner.getPredecessor().getNodeKey(),
              this.owner.getNodeKey(), Key.HashKeyBoundsForComparision.UPPER)) {
          System.out.println("Request has reached proper node.");
          this.handleRequestOnCurrentNode();
        } else {
            RemoteMethodsForCommWithinRing responsibleMember = this.owner.findSuccessor(request.getDataHashKey());
            this.forward(responsibleMember.getIp(), responsibleMember.getListenPort());
            System.out.println("Forwarded request to: " + responsibleMember.getIp() + ":" + responsibleMember.getListenPort());
        }
      } else {
          if (request.getDataHashKey().isBetween(this.owner.getNodeKey(),
                this.owner.getPredecessor().getNodeKey(), Key.HashKeyBoundsForComparision.UPPER)) {
            RemoteMethodsForCommWithinRing responsibleNode = this.owner.findSuccessor(request.getDataHashKey());
            this.forward(responsibleNode.getIp(), responsibleNode.getListenPort());
            System.out.println("Forwarded request to: " + responsibleNode.getIp() + ":" + responsibleNode.getListenPort());
          } else {
              System.out.println("Request has reached proper node.");
              this.handleRequestOnCurrentNode();
          }
      }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
      try {
        connection.getInputStream().close();
        connection.getOutputStream().close();
        connection.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void handleRequestOnCurrentNode() throws IOException
  {
    Object resp = null;
    ClusterDataOps dataOps = this.owner.getDataOps();
    if (request != null) {
      Object key = request.getKey();
      Object value = request.getPayload();
      switch (request.getRequestType()) {
        case "INSERT":
          dataOps.add(key, value);
          break;
        case "LOOKUP":
          Object lval = dataOps.get(key);
          resp = lval;
          break;
        case "UPDATE":
          dataOps.update(key, value);
          break;
        case "DELETE":
          Object rval = dataOps.remove(key);
          resp = rval;
          break;
      }
    }
    if (resp != null) {
      writeResponse(resp);
    } else {
      // write empty response
      writeResponse(null);
    }
  }

  public void writeResponse(Object payload) throws IOException
  {
    ServerResponse response = new ServerResponse();
    response.setRequestId(request.getRequestId());
    response.setRequestState("SUCCESS");
    response.setPayload(payload);

    if (request.isForwarded()) {
      Socket clientSocket = new Socket(request.getRespClientHost(), request.getRespClientPort());
      ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
      oos.writeObject(response);
      oos.flush();
      oos.close();
      clientSocket.close();
    } else {

      ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
      //write object to Socket
      oos.writeObject(response);
      //close resources
      oos.close();
    }
  }

  /**
   * Request is forwarded to the next appropriate chord member within the ring.
   * @param ip
   * @param port
   */
  public void forward(InetAddress ip, int port){
    try {
      request.setForwarded(true);
      Socket fSocket = new Socket(ip,port);
      ObjectOutputStream oos = new ObjectOutputStream(fSocket.getOutputStream());
      oos.writeObject(this.request);
      oos.flush();
      oos.close();
      fSocket.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void setRequest(ClientRequest request)
  {
    this.request = request;
  }


}


