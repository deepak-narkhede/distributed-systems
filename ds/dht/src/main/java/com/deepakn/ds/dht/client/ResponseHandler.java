package com.deepakn.ds.dht.client;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.deepakn.ds.dht.common.ServerResponse;

public class ResponseHandler implements Runnable
{
  private ServerSocket server;
  private int port;

  private boolean shutdown = false;

  private Map<String, ServerResponse> asyncResponses;

  public ResponseHandler(int port)
  {
    this.port = port;
    this.asyncResponses = new ConcurrentHashMap<>();
  }

  @Override
  public void run()
  {
    try {
      //create the socket server object
      server = new ServerSocket(port);
      //keep listens indefinitely until receives 'exit' call or program terminates
      while (true) {
        if (shutdown == true) {
          break;
        }

        System.out.println("Waiting for the client request");
        //creating socket and waiting for client connection
        Socket socket = server.accept();
        //read from socket to ObjectInputStream object
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        //convert ObjectInputStream object to String
        ServerResponse response = (ServerResponse)ois.readObject();
        System.out.println("Message Received: " + response);

        String clientId = getClientId(response);
        asyncResponses.put(clientId, response);
        ois.close();
        socket.close();
      }
      System.out.println("Shutting down Socket server!!");
      //close the ServerSocket object
      server.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public boolean isShutdown()
  {
    return shutdown;
  }

  public void setShutdown(boolean shutdown)
  {
    this.shutdown = shutdown;
  }

  public ServerSocket getServer()
  {
    return server;
  }

  public void setServer(ServerSocket server)
  {
    this.server = server;
  }

  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }

  public Map<String, ServerResponse> getAsyncResponses()
  {
    return asyncResponses;
  }

  public void setAsyncResponses(Map<String, ServerResponse> asyncResponses)
  {
    this.asyncResponses = asyncResponses;
  }

  private String  getClientId(ServerResponse response)
  {
    return response.getRequestId();
  }
}
