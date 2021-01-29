package com.deepakn.ds.dht;

import org.junit.Test;

import com.deepakn.ds.dht.client.api.DistributedHashTable;

public class ClientTest
{

  @Test
  public void sanity()
  {
    DistributedHashTable<String, String> dht = new DistributedHashTable<>();
    dht.add("TestKey", "TestValue");
  }

}
