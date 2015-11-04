package org.renjin.gnur.api;

/**
 * Exception thrown by unimplemented GNU R API methods
 */
public class UnimplementedGnuApiMethod extends RuntimeException {
  
  UnimplementedGnuApiMethod(String functionName) {
    super("Unimplemented GNU R API function '" + functionName + "'");
  }
}
