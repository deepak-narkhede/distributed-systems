package com.deepakn.ds.dht.common;


import java.io.Serializable;

import com.deepakn.ds.dht.cluster.utils.Key;

public class ClientRequest implements Serializable {

  private String requestId;
  private String requestType;
  private Key dataHashKey;

  private Object key;
  private Object payload;

  private String respClientHost;
  private int respClientPort;

  private boolean forwarded;

  public ClientRequest(String requestType, Object key, Object payload)
  {
    this.requestType = requestType;
    this.key = key;
    this.payload = payload;
    this.setDataHashKey(new Key((String)key));
  }

  public String getRespClientHost()
  {
    return respClientHost;
  }

  public void setRespClientHost(String respClientHost)
  {
    this.respClientHost = respClientHost;
  }

  public int getRespClientPort()
  {
    return respClientPort;
  }

  public void setRespClientPort(int respClientPort)
  {
    this.respClientPort = respClientPort;
  }

  public ClientRequest(String requestType, Object key)
  {
    this(requestType, key, null);
  }

  public Key getDataHashKey()
  {
    return dataHashKey;
  }

  public void setDataHashKey(Key dataHashKey) {
    this.dataHashKey = dataHashKey;
  }

  public Object getKey()
  {
    return key;
  }

  public void setKey(Object key)
  {
    this.key = key;
  }

  public Object getPayload()
  {
    return payload;
  }

  public String getRequestType()
  {
    return requestType;
  }

  public String getRequestId()
  {
    return requestId;
  }

  public boolean isForwarded()
  {
    return forwarded;
  }

  public void setForwarded(boolean forwarded)
  {
    this.forwarded = forwarded;
  }

  public void setRequestId(String requestId)
  {
    this.requestId = requestId;
  }

  public void setRequestType(String requestType)
  {
    this.requestType = requestType;
  }

  public void setPayload(Object payload)
  {
    this.payload = payload;
  }
}

