package com.deepakn.ds.dht.cluster;

public interface BaseClusterOps
{
  boolean join(String ringId);

  boolean leave();

}
