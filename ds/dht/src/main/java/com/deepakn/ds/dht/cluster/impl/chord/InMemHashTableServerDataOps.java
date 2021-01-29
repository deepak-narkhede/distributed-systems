package com.deepakn.ds.dht.cluster.impl.chord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.deepakn.ds.dht.cluster.ClusterDataOps;

public class InMemHashTableServerDataOps<K, V> implements ClusterDataOps<K, V>
{
  private Map<K ,V> data;

  @Override
  public void setup()
  {
    if (data != null) {
      data = new ConcurrentHashMap<>();
    }
  }

  @Override
  public void add(K key, V value)
  {
    data.put(key, value);
  }

  @Override
  public V get(K key)
  {
    return data.get(key);
  }

  @Override
  public V remove(K key)
  {
    return data.remove(key);
  }

  @Override
  public void update(K key, V value)
  {
    data.put(key, value);
  }
}
