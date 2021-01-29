package com.deepakn.ds.dht.cluster.impl.chord;

import com.deepakn.ds.dht.cluster.RemoteMethodsForCommWithinRing;

/**
 * Chord protocol uses finger table for making fast hops to other members.
 */
public class FingerTable
{
  private RemoteMethodsForCommWithinRing[] fingers;

  public FingerTable(int length)
  {
    this.fingers = new RemoteMethodsForCommWithinRing[length];
  }

  public void put(RemoteMethodsForCommWithinRing finger , int i)
  {
    synchronized(this.fingers) {
      this.fingers[i] = finger;
    }
  }

  public int length()
  {
    return fingers.length;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for(RemoteMethodsForCommWithinRing f : this.fingers) {
      sb.append(f.toString());
    }
    return sb.toString();
  }

  public RemoteMethodsForCommWithinRing[] getFingers()
  {
    return this.fingers;
  }
}

