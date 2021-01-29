package com.deepakn.ds.dht.common;

public class ServerResponse
{
  private String requestState;
  private String requestId;
  private Object payload;

  public String getRequestState()
  {
    return requestState;
  }

  public void setRequestState(String requestState)
  {
    this.requestState = requestState;
  }

  public Object getPayload()
  {
    return payload;
  }

  public void setPayload(Object payload)
  {
    this.payload = payload;
  }

  public String getRequestId()
  {
    return requestId;
  }

  public void setRequestId(String requestId)
  {
    this.requestId = requestId;
  }
}
