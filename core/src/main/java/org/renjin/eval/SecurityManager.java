package org.renjin.eval;

/**
 * Enforces security rules for a specific R context. 
 * Note this is just a quick spike to make sure that AppEngine demo 
 * does not allow unfettered access to the instance. We probably
 * just need to figure out how to apply java's own security management
 * API to renjin.
 */
public class SecurityManager {

  public boolean allowNewInstance(Class clazz) {
    return true;
  }
  
}
