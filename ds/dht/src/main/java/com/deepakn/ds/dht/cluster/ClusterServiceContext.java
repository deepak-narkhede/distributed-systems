package com.deepakn.ds.dht.cluster;

import java.util.HashMap;
import java.util.Map;

public class ClusterServiceContext
{
  public static String CLUSTER_CONTEXT_NODE_LISTEN_PORT = "node.port";
  public static String CLUSTER_CONTEXT_RING_NODE_HOSTNAME_AND_PORT_KEY = "ring.node.hostnameAndPort";
  public static final String CLUSTER_CONTEXT_NODE_KEY = "node.key";


  Map<String, Object> properties;
  public void addProp(String key, Object val)
  {
    if (properties == null) {
      properties = new HashMap<>();
    }
    properties.put(key, val);
  }

  public Object getProp(String key)
  {
    if (properties != null) {
      return properties.get(key);
    }
    return null;
  }

}
