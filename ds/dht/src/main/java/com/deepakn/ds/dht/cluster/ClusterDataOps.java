package com.deepakn.ds.dht.cluster;

public interface ClusterDataOps<K,V>
{
  void setup();

  void add(K key, V value);

  V get(K key);

  V remove(K key);

  void update(K key, V value);
}
