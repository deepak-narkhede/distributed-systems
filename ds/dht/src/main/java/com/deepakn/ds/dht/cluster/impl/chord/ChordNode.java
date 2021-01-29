package com.deepakn.ds.dht.cluster.impl.chord;

import java.rmi.server.UnicastRemoteObject;

import com.deepakn.ds.dht.cluster.ClusterDataOps;
import com.deepakn.ds.dht.cluster.Member;
import com.deepakn.ds.dht.cluster.RemoteMethodsForCommWithinRing;
import com.deepakn.ds.dht.cluster.utils.Hash;
import com.deepakn.ds.dht.cluster.utils.Key;

import java.math.BigInteger;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.*;


/**
 * This class represents a chord member which rmi interface to communicate with other members.
 */
public class ChordNode extends UnicastRemoteObject implements RemoteMethodsForCommWithinRing, Member
{

  // Chord member which handles the incoming requests.
  private ChordServer server;

  // unique id of the chord member within the ring.
  private Key nodeKey;

  // ip address of the current node where chord server would run.
  private InetAddress ip;

  // port of the node where chord server listens for requests.
  private int listenPort;

  // data operations on chord member
  private ClusterDataOps dataOps;

  // finger table for faster access within the ring used by chord protocol.
  private FingerTable fingerTable;

  // predecessor member as per hash key values.
  private RemoteMethodsForCommWithinRing predecessor;

  // successor member as per hash key values.
  private RemoteMethodsForCommWithinRing successor;

  // current status of the chord member node.
  private boolean isAlive;

  // periodically stabilizes the information based on cluster operation
  private Stabilizer stabilizer;

  // timer task for invocation of stabilization process.
  private Timer stabilizeTimer;


  public ChordNode(InetAddress ip, int lPort) throws RemoteException
  {
    this(ip, lPort, new InMemHashTableServerDataOps());
  }

  public ChordNode(InetAddress ip, int listenPort, ClusterDataOps dataOps) throws RemoteException
  {
    this.ip = ip;
    this.listenPort = listenPort;
    this.nodeKey = new Key(ip.getHostAddress() + ":" + listenPort);
    this.server = new ChordServer(this);
    this.fingerTable = new FingerTable(Hash.getKeyLen());
    this.stabilizer = new Stabilizer(this);
    this.stabilizeTimer = new Timer();
    this.isAlive = true;
    this.dataOps = dataOps;
  }


  // -- Methods used for operations on chord member like join/leave or balancing the ring. --

  /**
   * Join the chord ring by fetching appropriate neighbours of node
   * @param node
   */
  public void join(RemoteMethodsForCommWithinRing node) throws RemoteException
  {
    if (this.equals(node)) {
      this.predecessor = this;
      this.successor = this;
      for (int i = 0; i < this.fingerTable.length(); i++) {
        this.fingerTable.put(this, i);
      }
    } else {
      this.initFingerTable(node);
      this.updateOthers();
      this.successor.offloadData(this);
    }
    //TODO: take config for stabilization as context.
    this.stabilizeTimer.scheduleAtFixedRate(this.stabilizer,10000,5000);
  }

  /**
   * Leave the chord ring gracefully also balance the ring by offloading.
   */
  public void leave() throws RemoteException
  {
    this.server.stop();
    this.stabilizeTimer.cancel(); // TODO: update fingers of others
    this.successor.setPredecessor(this.predecessor);
    this.predecessor.setSuccessor(this.successor);
    this.offloadData(this.successor);
  }

  /**
   * Initialize the fingers or rather hop table to other members using node.
   * @param node
   */
  public void initFingerTable(RemoteMethodsForCommWithinRing node)
  {
    try {
      this.successor = node.findSuccessor(this.getNodeKey());
      this.fingerTable.put(this.successor,0);
      this.predecessor = this.successor.getPredecessor();
      this.successor.setPredecessor(this);
      this.predecessor.setSuccessor(this);
      for (int i = 1; i < this.fingerTable.length(); i++) {
        Key nextFingerKey = this.successor(i);
        boolean isBetweenThisAndFinger = nextFingerKey.isBetween(this.nodeKey,
            this.fingerTable.getFingers()[i-1].getNodeKey(), Key.HashKeyBoundsForComparision.LOWER);
        if (isBetweenThisAndFinger) {
          this.fingerTable.put(this.fingerTable.getFingers()[i-1], i);
        } else {
          RemoteMethodsForCommWithinRing finger = node.findSuccessor(nextFingerKey);
          if (!finger.equals(this)) {
            this.fingerTable.put(finger, i);
          } else {
            this.fingerTable.put(this.successor,i);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Update information in finger tables of other members within the ring.
   */
  public void updateOthers() throws RemoteException
  {
    this.predecessor.setFinger(this,0);
    for (int i = 0; i < this.fingerTable.length(); i++) {
      try {
        Key predFingerKey = this.predecessor(i);
        RemoteMethodsForCommWithinRing p;
        p = this.findPredecessor(predFingerKey);
        if (!p.equals(this)){
          p.updateFingerTable(this, i);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public synchronized void updateFingerTable(RemoteMethodsForCommWithinRing s, int i)
      throws RemoteException
  {
    if (s.getNodeKey().isBetween(this.nodeKey, this.fingerTable.getFingers()[i].getNodeKey(),
          Key.HashKeyBoundsForComparision.LOWER)) {
      this.fingerTable.put(s, i);
      this.predecessor.updateFingerTable(s, i);
    }
  }

  /**
   * Periodically check and verify immediate successor and correct if possible.
   */
  public void stabilize() throws RemoteException
  {
    RemoteMethodsForCommWithinRing x = this.successor.getPredecessor();
    if(x.getNodeKey().isBetween(this.nodeKey,this.successor.getNodeKey(),
        Key.HashKeyBoundsForComparision.NONE)) {
      this.setSuccessor(x);
      this.successor.notify(this);
    }
  }

  /**
   * Notifier is closer than the current then fix the predecessor.
   */
  public void notify(RemoteMethodsForCommWithinRing candidatePredecessor) throws RemoteException
  {
    if (this.predecessor == null ||  candidatePredecessor.getNodeKey().isBetween(
          this.predecessor.getNodeKey(), this.getNodeKey(), Key.HashKeyBoundsForComparision.NONE)) {
      this.setPredecessor(candidatePredecessor);
    }
  }

  /**
   * Refresh finger table entries periodically.
   */
  public void fixFingers() throws RemoteException{
    int i = (int)(this.fingerTable.length()*Math.random());
    if (i>0 && this.fingerTable.getFingers()[i] != null) {
      this.fingerTable.put(this.findSuccessor(this.fingerTable.getFingers()[i].getNodeKey()),i);
    }
  }

  /**
   * Checks whether predecessor has failed
   */
  public void checkPredecessor() throws RemoteException {
    if (this.predecessor!= null && !this.predecessor.isAlive()) {
      this.predecessor = null;
    }
  }

  /**
   * Get the status of current chord member.
   * @return
   */
  public boolean isAlive(){
    return this.isAlive;
  }

  /**
   * Transfer data to other members in case of node join or leave.
   * @param responsible
   */
  public void offloadData(RemoteMethodsForCommWithinRing responsible) throws RemoteException
  {
    //TODO: Implement
  }


   // -- Helper methods for key calculation used for entries of the finger table. --

  /**
   * ith entry calculation of the fingerTable.
   * @param i entry index
   * @return key value.
   */
  private Key successor(int i)
  {
    BigInteger rterm = BigInteger.valueOf(2);
    rterm = rterm.pow(i);
    BigInteger fingerKey = this.nodeKey.toBigInt().add(rterm);
    BigInteger divisor = BigInteger.valueOf(2);
    divisor = divisor.pow(Hash.getKeyLen());
    fingerKey = fingerKey.mod(divisor);
    return new Key(fingerKey.toByteArray());
  }

  /**
   * ith key calculation of incoming neighbours to this member.
   * @param i entry index
   * @return key value.
   */
  private Key predecessor(int i)
  {
    BigInteger rterm = BigInteger.valueOf(2);
    rterm = rterm.pow(i);
    BigInteger fingerKey = this.nodeKey.toBigInt().subtract(rterm);
    if (fingerKey.compareTo(BigInteger.ZERO)== -1){
      BigInteger maxKeyVal = BigInteger.valueOf(2);
      maxKeyVal = maxKeyVal.pow(Hash.getKeyLen());
      fingerKey = maxKeyVal.add(fingerKey).add(BigInteger.ONE);
    }
    return new Key(fingerKey.toByteArray());
  }


  // -- Methods to search within the chord ring based on provided key. --

  /**
   * Finds the successor member of the provided key
   * @param key key
   * @return  successor
   */
  public RemoteMethodsForCommWithinRing findSuccessor(Key key) throws RemoteException
  {
    return findPredecessor(key).getSuccessor();
  }

  /**
   * Finds the predecessor member of the provided key
   * @param key key
   * @return the successor
   */
  public RemoteMethodsForCommWithinRing findPredecessor(Key key) throws RemoteException
  {
    RemoteMethodsForCommWithinRing n = this;
    RemoteMethodsForCommWithinRing lastCandidatePred = this;
    while (!key.isBetween(n.getNodeKey(), n.getSuccessor().getNodeKey(), Key.HashKeyBoundsForComparision.UPPER)) {
      n = n.closestPrecedingFinger(key);
      //if we meet the same node twice then the new key doesn't belong to the current intervals
      //so it must either be the minimum or the maximum in the ring(either way it's pred is the current max key)
      if (n.equals(lastCandidatePred)) {
        return n.findMax();
      }
      lastCandidatePred = n;
    }
    return n;
  }

  /**
   * Find the the closest preceding member by provided key using finger table.
   * @param key key
   * @return closest predecessor
   */
  public RemoteMethodsForCommWithinRing closestPrecedingFinger(Key key) throws RemoteException
  {
    for(int i=this.fingerTable.length()-1; i>=0; i--){
      if(this.nodeKey.equals(this.nodeKey.min(this.fingerTable.getFingers()[i].getNodeKey()))){
        if(this.fingerTable.getFingers()[i].getNodeKey().isBetween(this.nodeKey,key, Key.HashKeyBoundsForComparision.NONE)){
          return this.fingerTable.getFingers()[i];
        }
      }
      else {
        if (key.isBetween(this.fingerTable.getFingers()[i].getNodeKey(),this.nodeKey, Key.HashKeyBoundsForComparision.NONE)){
          return this.fingerTable.getFingers()[i];
        }
      }
    }
    return this;
  }

  /**
   * Get the chord member with the maximum key within the ring.
   * @return
   */
  public RemoteMethodsForCommWithinRing findMax() throws RemoteException
  {
    for(int i = this.fingerTable.length() - 1; i >= 0; i--) {
      RemoteMethodsForCommWithinRing finger = this.fingerTable.getFingers()[i];
      if (!this.equals(finger)) {
        if (finger.getNodeKey().equals(finger.getNodeKey().max(this.nodeKey))) {
          return finger.findMax();
        }
      }
    }
    return this;
  }

  public InetAddress getIp()
  {
    return this.ip;
  }

  public int getListenPort()
  {
    return this.listenPort;
  }

  public ChordServer getServer()
  {
    return server;
  }

  public Key getNodeKey()
  {
    return this.nodeKey;
  }

  public RemoteMethodsForCommWithinRing getSuccessor()
  {
    return successor;
  }

  public synchronized void setSuccessor(RemoteMethodsForCommWithinRing successor)
  {
    this.successor = successor;
  }

  public RemoteMethodsForCommWithinRing getPredecessor()
  {
    return predecessor;
  }

  public synchronized void setPredecessor(RemoteMethodsForCommWithinRing predecessor)
  {
    this.predecessor = predecessor;
  }

  public synchronized void setFinger(RemoteMethodsForCommWithinRing finger,int i)
  {
    if (finger.equals(this.successor)) {
      this.fingerTable.put(finger, i);
    }
  }

  public String printFingerTable() throws RemoteException
  {
    StringBuilder sb = new StringBuilder();
    sb.append("----- FingerTable -------");
    int entry = 0;
    for (RemoteMethodsForCommWithinRing f: this.fingerTable.getFingers()) {
      sb.append("Entry: ").append(entry).append("=>").append(f.getNodeKey().toBigInt());
      entry++;
    }
    return sb.toString();
  }

  @Override
  public int hashCode()
  {
    int hashCode = 1;
    hashCode = hashCode * 37 + this.listenPort;
    hashCode = hashCode * 23 + this.ip.hashCode();

    return hashCode;
  }


  @Override
  public String toString()
  {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("Node: ").append(ip.toString()).append(":").append(listenPort);
      sb.append("key: ").append(getNodeKey().toBigInt());
      sb.append("Pred: ").append(predecessor.getNodeKey().toBigInt());
      sb.append("Succ: ").append(successor.getNodeKey().toBigInt());
      sb.append(printFingerTable());

      return sb.toString();
    } catch (RemoteException re) {
      return re.getMessage();
    }
  }

  public void notifyUpdates() throws RemoteException
  {
    //TODO: right now just print them
    printUpdates();
  }

  public ClusterDataOps getDataOps()
  {
    return dataOps;
  }

  public void setDataOps(ClusterDataOps dataOps)
  {
    this.dataOps = dataOps;
  }

  private void printUpdates()throws RemoteException
  {
    BigInteger predKey = this.predecessor.getNodeKey().toBigInt();
    BigInteger succKey = this.successor.getNodeKey().toBigInt();
    System.out.println("Updates => key: " + this.nodeKey.toBigInt() + " Pred: " + predKey + "Succ: " + succKey);
  }

}