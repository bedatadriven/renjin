package org.renjin.gcc;

/**
 * Thrown when problems occur importing external symbols
 */
public class ImportException extends RuntimeException {

  public ImportException() {
  }

  public ImportException(String message) {
    super(message);
  }

  public ImportException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImportException(Throwable cause) {
    super(cause);
  }

  public ImportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
