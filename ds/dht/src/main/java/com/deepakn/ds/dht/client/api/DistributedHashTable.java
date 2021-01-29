package com.deepakn.ds.dht.client.api;

import com.deepakn.ds.dht.client.BaseHashTable;
import com.deepakn.ds.dht.common.ClientRequest;
import com.deepakn.ds.dht.client.ClientContext;
import com.deepakn.ds.dht.client.ClientHandle;

public class DistributedHashTable<K, V> extends BaseHashTable<K, V>
{
  private transient ClientHandle cHandle;

  public DistributedHashTable(String sHost, Integer sPort)
  {
    ClientContext context = new ClientContext();
    context.setProp(ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_HOSTNAME_KEY, sHost);
    context.setProp(ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_PORT_KEY, sPort);
    context.setProp(ClientContext.CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_KEY,
      ClientContext.CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_VAL);

    ClientHandle cHandle = new ClientHandle(context);
    this.cHandle = cHandle;
  }

  public DistributedHashTable(ClientContext context)
  {
    ClientHandle cHandle = new ClientHandle(context);
    this.cHandle = cHandle;
  }

  public DistributedHashTable()
  {
    ClientContext context = new ClientContext();
    context.setProp(ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_HOSTNAME_KEY,
        ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_HOSTNAME_VAL);
    context.setProp(ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_PORT_KEY,
        ClientContext.CLIENT_CONTEXT_DEFAULT_CHORD_PORT_VAL);
    context.setProp(ClientContext.CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_KEY,
        ClientContext.CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_VAL);

    ClientHandle cHandle = new ClientHandle(context);
    this.cHandle = cHandle;
  }

  @Override
  public void setup()
  {

  }

  @Override
  public void add(K key, V value)
  {
    ClientRequest request = cHandle.createRequest("ADD", key, value);
    cHandle.sendRequestAndReadResponse(request);
  }

  @Override
  public V get(K key)
  {
    ClientRequest request = cHandle.createRequest("GET", key, null);
    Object resp = cHandle.sendRequestAndReadResponse(request);
    if (resp != null) {
      return (V)resp;
    } else {
      System.out.println("Key not found in DHT");
      return null;
    }
  }

  @Override
  public V remove(K key)
  {
    ClientRequest request = cHandle.createRequest("REMOVE", key, null);
    Object resp = cHandle.sendRequestAndReadResponse(request);
    if (resp != null) {
      return (V)resp;
    } else {
      System.out.println("Key not found in DHT");
      return null;
    }
  }

  @Override
  public void update(K key, V value)
  {
    ClientRequest request = cHandle.createRequest("UPDATE", key, null);
    cHandle.sendRequestAndReadResponse(request);
  }

}
