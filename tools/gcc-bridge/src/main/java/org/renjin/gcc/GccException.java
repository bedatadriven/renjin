package org.renjin.gcc;

public class GccException extends RuntimeException {

  public GccException(String message) {
    super(message);
  }

  public GccException(String message, Exception e) {
    super(message, e);
  }
}
