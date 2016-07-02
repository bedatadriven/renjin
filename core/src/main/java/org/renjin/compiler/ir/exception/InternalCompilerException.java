package org.renjin.compiler.ir.exception;

/**
 * Indicates that something unexpected went wrong during the compilation.
 */
public class InternalCompilerException extends RuntimeException {

  public InternalCompilerException() {
  }

  public InternalCompilerException(String message) {
    super(message);
  }

  public InternalCompilerException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalCompilerException(Throwable cause) {
    super(cause);
  }

  public InternalCompilerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
