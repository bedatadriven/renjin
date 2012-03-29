package org.renjin.eval;


/**
 * Provides implementations for the session-oriented R commands
 * like quit(), browse(), etc
 */
public interface SessionController {
  
  public enum SaveMode {
    NO,
    YES,
    ASK,
    DEFAULT
  }

  public void quit(Context context, SaveMode saveMode, int exitCode, boolean runLast );
  
  
  
  
  
}
