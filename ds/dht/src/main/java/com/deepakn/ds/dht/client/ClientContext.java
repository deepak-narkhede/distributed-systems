package com.deepakn.ds.dht.client;

import java.util.HashMap;
import java.util.Map;

public class ClientContext
{
  public static String CLIENT_CONTEXT_DEFAULT_CHORD_HOSTNAME_KEY = "client.node.hostname.default";
  public static String CLIENT_CONTEXT_DEFAULT_CHORD_PORT_KEY = "client.node.port.default";
  public static String CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_KEY = "client.node.data.resp.port.default";

  public static String CLIENT_CONTEXT_DEFAULT_CHORD_HOSTNAME_VAL = "localhost";
  public static int CLIENT_CONTEXT_DEFAULT_CHORD_PORT_VAL = 9999;
  public static int CLIENT_CONTEXT_DEFAULT_DATA_RESP_PORT_VAL = 9998;

  Map<String, Object> props;

  public void setProp(String key, Object val)
  {
    if (props == null) {
      props = new HashMap<>();
    }
    props.put(key, val);
  }

  public Object getProp(String key)
  {
    if (props != null) {
      return props.get(key);
    }

    return null;
  }

}
