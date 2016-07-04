package org.renjin.compiler.builtins;

/**
 * Thrown when specialization failed to resolve a function call to
 * a predictable result.
 */
public class FailedToSpecializeException extends RuntimeException {

  public FailedToSpecializeException() {
  }

  public FailedToSpecializeException(String message) {
    super(message);
  }
}
