package r.lang.exception;


public class IncompatibleDimensionsException extends RuntimeException {
 
   public IncompatibleDimensionsException(Throwable cause) {
    super(cause);
  }

  public IncompatibleDimensionsException(String message, Throwable cause) {
    super(message, cause);
  }

  public IncompatibleDimensionsException(String message) {
    super(message);
  }

  public IncompatibleDimensionsException() {
  }
  
  
}
