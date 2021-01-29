package com.deepakn.ds.dht.cluster.impl.chord;

import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.deepakn.ds.dht.cluster.BaseClusterOps;
import com.deepakn.ds.dht.cluster.ClusterServiceContext;
import com.deepakn.ds.dht.cluster.RemoteMethodsForCommWithinRing;
import com.deepakn.ds.dht.cluster.utils.Key;

public class ChordManagedService implements BaseClusterOps
{
  private ClusterServiceContext context;
  private transient ChordNode currentNode;

  public ChordManagedService()
  {

  }

  public ChordManagedService(ClusterServiceContext context)
  {
    this.context = context;
  }

  public static void main(String[] args)
  {
    CommandLine cmd = getCommandLine(args);
    int lPort = Integer.parseInt(cmd.getOptionValue("port"));
    String ringHostAndPort = cmd.getOptionValue("ringHostAndPort");

    ChordManagedService cService = new ChordManagedService();
    cService.setup(ringHostAndPort, lPort);
    cService.joinAndStart();
  }


  public void setup(String ringNodeHostAndPort, int currentNodeListenPort)
  {
    ClusterServiceContext context = new ClusterServiceContext();
    if (ringNodeHostAndPort != null && !ringNodeHostAndPort.isEmpty()) {
      context.addProp(ClusterServiceContext.CLUSTER_CONTEXT_RING_NODE_HOSTNAME_AND_PORT_KEY, ringNodeHostAndPort);
    }
    context.addProp(ClusterServiceContext.CLUSTER_CONTEXT_NODE_LISTEN_PORT, currentNodeListenPort);

    this.context = context;
  }

  public void joinAndStart() {
    try {
      if (context == null) {
        throw new RuntimeException("Please initialize the context using setup");
      }
      String ringId = (String)context.getProp(ClusterServiceContext.CLUSTER_CONTEXT_RING_NODE_HOSTNAME_AND_PORT_KEY);
      boolean res = join(ringId);
      if (res != true) {
        System.out.println("Unable to join the ring or create a new one.");
      }
    } catch (Exception e) {
      System.out.println("Error occurred while joining the node to new ring or existing ring. Exception: " + e.getMessage());
      throw new RuntimeException("Error while joining the node to ring");
    }
  }

  @Override
  public boolean join(String ringId)
  {
    if (context == null) {
      throw new RuntimeException("Please initialize the context using setup");
    }

    int lPort = (int)context.getProp(ClusterServiceContext.CLUSTER_CONTEXT_NODE_LISTEN_PORT);
    ChordNode cNode = internalJoin(ringId, lPort);
    if (cNode != null) {
      try {
        startChordServer(cNode);
        context.addProp(ClusterServiceContext.CLUSTER_CONTEXT_NODE_KEY, cNode.getNodeKey());
        currentNode = cNode;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }
    return false;
  }


  @Override
  public boolean leave()
  {
    if (currentNode == null) {
      System.out.println("Currently node is not joined to a particular ring.");
      return false;
    }
    try {
      currentNode.leave();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return true;
  }

  private ChordNode internalJoin(String ringId, int lPort)
  {
    ChordNode cNode = null;
    try {
      cNode = new ChordNode(InetAddress.getLocalHost(), lPort);
      if (ringId != null && !ringId.isEmpty()) {
        // now join the existing ring
        Key ringKey = new Key(ringId);
        String[] split = ringId.split(":");
        System.out.println("Seeking remote registry at" + split[0] + ":" + Registry.REGISTRY_PORT + " ...");
        RemoteMethodsForCommWithinRing remoteNodeHandler = getRemoteHandler(split[0], ringKey);
        cNode.join(remoteNodeHandler);
        remoteNodeHandler.notifyUpdates();
      } else {
        // start a new ring and self join
        cNode.join(cNode);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return cNode;
  }

  private RemoteMethodsForCommWithinRing getRemoteHandler(String remoteNode, Key ringKey) throws RemoteException, NotBoundException
  {
    Registry remoteRegistry = null;
    try {
      remoteRegistry = LocateRegistry.getRegistry(remoteNode, Registry.REGISTRY_PORT);
    } catch (RemoteException e) {
      e.printStackTrace();
      throw new RuntimeException("Remote registry not found.");
    }
    System.out.println("Registry found, locating remote object...");
    RemoteMethodsForCommWithinRing remoteNodeHandler =
        (RemoteMethodsForCommWithinRing)remoteRegistry.lookup(ringKey.toHex());
    System.out.println("Remote Node: " + remoteNodeHandler);

    return remoteNodeHandler;
  }

  private void startChordServer(ChordNode cNode) throws AlreadyBoundException, RemoteException
  {
    Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
    registry.bind(cNode.getNodeKey().toHex(), cNode);
    System.setProperty("java.rmi.hostname", cNode.getIp().getHostAddress());
    cNode.getServer().start();
  }

  private static CommandLine getCommandLine(String[] args)
  {
    CommandLine cmd = null;
    Options options = new Options();
    Option input = new Option("p", "port", true, "Chord server listen port");
    input.setRequired(true);
    options.addOption(input);

    input = new Option("r", "ringHostAndPort", true, "Specify Chord ring hostname and port eg: localhost:9999");
    input.setRequired(false);
    options.addOption(input);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("ChordManagedService", options);
      System.exit(1);
    }

    return cmd;

  }


}
