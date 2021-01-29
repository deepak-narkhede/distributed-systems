package com.deepakn.ds.dht.cluster;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.deepakn.ds.dht.cluster.utils.Key;

/**
 * RMI Interface used by the chord members of the ring to communicate with each other.
 */
public interface RemoteMethodsForCommWithinRing extends Remote
{

  RemoteMethodsForCommWithinRing findSuccessor(Key key)throws RemoteException;

  RemoteMethodsForCommWithinRing findPredecessor(Key key)throws RemoteException;

  RemoteMethodsForCommWithinRing closestPrecedingFinger(Key key)throws RemoteException;

  void updateFingerTable(RemoteMethodsForCommWithinRing node, int i) throws RemoteException;

  RemoteMethodsForCommWithinRing findMax() throws RemoteException;

  void offloadData(RemoteMethodsForCommWithinRing responsible) throws RemoteException;

  void notify(RemoteMethodsForCommWithinRing candidatePredecessor) throws RemoteException;

  InetAddress getIp() throws RemoteException;

  int getListenPort() throws RemoteException;

  Key getNodeKey() throws RemoteException;

  RemoteMethodsForCommWithinRing getSuccessor() throws RemoteException;

  void setSuccessor(RemoteMethodsForCommWithinRing successor) throws RemoteException;

  RemoteMethodsForCommWithinRing getPredecessor()throws RemoteException;

  void setPredecessor(RemoteMethodsForCommWithinRing predecessor) throws RemoteException;

  void setFinger(RemoteMethodsForCommWithinRing finger, int index) throws RemoteException;

  boolean isAlive() throws RemoteException;

  void notifyUpdates() throws RemoteException;


}

