package org.renjin.invoke.annotations.processor;

public class ArgumentException extends RuntimeException {

  public ArgumentException(String message) {
    super(message);
  }
}
