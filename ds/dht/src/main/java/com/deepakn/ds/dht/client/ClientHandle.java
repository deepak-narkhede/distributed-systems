package com.deepakn.ds.dht.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import com.deepakn.ds.dht.common.ClientRequest;
import com.deepakn.ds.dht.common.ServerResponse;

public class ClientHandle
{
  private ClientContext context;
  private transient Socket serverSocket;

  private ResponseHandler responseHandler;

  public ClientHandle(ClientContext context)
  {
    String chordHost = (String)context.getProp(ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_HOSTNAME_KEY);
    int chordPort = (int)context.getProp(ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_PORT_KEY);
    int respPort = (int)context.getProp(ClientContext.CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_KEY);
    this.context = context;

    try {
      serverSocket = new Socket(chordHost, chordPort);
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.responseHandler = new ResponseHandler(respPort);
    Thread service = new Thread(responseHandler);
    service.start();
  }

  public ClientRequest createRequest(String opType, Object key, Object payload)
  {
    ClientRequest request = new ClientRequest(opType, key, payload);
    request.setRequestId(UUID.randomUUID().toString());
    request.setRespClientHost(responseHandler.getServer().getInetAddress().getHostName());
    request.setRespClientPort(responseHandler.getPort());

    return request;
  }


  public Object sendRequestAndReadResponse(ClientRequest request)
  {
    ServerResponse response = sendRequest(request);
    if (response != null) {
      Object dataPayload = readResponse(request, response);
      return dataPayload;
    }

    return null;
  }


  public ServerResponse sendRequest(ClientRequest request)
  {
    ServerResponse resp = null;
    try {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;

      //establish socket connection to server
      //write to socket using ObjectOutputStream
      oos = new ObjectOutputStream(getServerSocket().getOutputStream());
      System.out.println("Sending request to Socket ChordServer");
      oos.writeObject(request);
      //read the server response message
      ois = new ObjectInputStream(getServerSocket().getInputStream());

      resp = (ServerResponse) ois.readObject();

      System.out.println("Message: " + resp);

      //close resources
      ois.close();
      oos.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return resp;
  }


  public Object readResponse(ClientRequest request, ServerResponse response)
  {
    if (response.getRequestState().equals("SUCCESS")) {
      return response.getPayload();
    }

    Object data = waitForDataResponse(request, response);

    return data;

  }

  private Object waitForDataResponse(ClientRequest request, ServerResponse response)
  {
    String requestId = request.getRequestId();
    while (responseHandler.getAsyncResponses().get(requestId) == null) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return responseHandler.getAsyncResponses().remove(requestId);
  }

  public Socket getServerSocket()
  {
    return serverSocket;
  }

  public void setServerSocket(Socket serverSocket)
  {
    this.serverSocket = serverSocket;
  }
}
