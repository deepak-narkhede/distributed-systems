package com.deepakn.ds.dht.client;

import com.deepakn.ds.dht.cluster.ClusterDataOps;

public abstract class BaseHashTable<K, V> implements ClusterDataOps<K, V>
{
  @Override
  public void setup()
  {

  }

  @Override
  public void add(K key, V value)
  {

  }

  @Override
  public V get(K key)
  {
    return null;
  }

  @Override
  public V remove(K key)
  {
    return null;
  }

  @Override
  public void update(K key, V value)
  {

  }

}
