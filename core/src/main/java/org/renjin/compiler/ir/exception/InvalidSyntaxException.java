package org.renjin.compiler.ir.exception;

/**
 * Indicates that the intermediate form could not be build because
 * of an error in the input syntax tree.
 */
public class InvalidSyntaxException extends RuntimeException {

  public InvalidSyntaxException(String message) {
    super(message);
  }

}
