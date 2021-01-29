package com.deepakn.ds.dht.cluster.impl.chord;


import java.rmi.RemoteException;
import java.util.TimerTask;

/**
 * Stabilization is the process to refresh the entries and fix finger tables periodically.
 */
public class Stabilizer extends TimerTask
{
  private ChordNode self;

  public Stabilizer(ChordNode self)
  {
    this.self = self;
  }

  @Override
  public void run()
  {
    try {
      self.checkPredecessor();
      self.stabilize();
      self.fixFingers();
    } catch (RemoteException re) {
      re.getMessage();
    }
  }
}

