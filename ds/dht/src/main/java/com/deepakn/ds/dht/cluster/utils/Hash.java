package com.deepakn.ds.dht.cluster.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class Hash {

  private static String DEFAULT_HASH_ALGO = "SHA-1";
  private static int DEFAULT_HASH_KEY_LEN = 160;

  private static String hashAlgo = DEFAULT_HASH_ALGO;
  private static int keyLen = DEFAULT_HASH_KEY_LEN;

  private static byte[] hashValue;

  public static void hash(String id)
  {
    try {
      MessageDigest md = MessageDigest.getInstance(hashAlgo);
      md.reset();
      Hash.hashValue = md.digest(id.getBytes());
      Hash.setKeyLen(hashValue.length * 8);
    }
    catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

  }

  public static byte[] getmBitsHashValue(String id)
  {
    try {
      MessageDigest md = MessageDigest.getInstance(hashAlgo);
      md.reset();
      Hash.hashValue = md.digest(id.getBytes());
      Hash.setKeyLen(hashValue.length * 8);
      return Arrays.copyOf(Hash.hashValue, hashValue.length);
    }
    catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static byte[] getHashValue()
  {
    return Arrays.copyOf(Hash.hashValue, hashValue.length);
  }

  public static void setHashAlgo(String name)
  {
    Hash.hashAlgo = name;
  }

  public static int getKeyLen()
  {
    return keyLen;
  }

  public static void setKeyLen(int length)
  {
    keyLen = length;
  }

}

