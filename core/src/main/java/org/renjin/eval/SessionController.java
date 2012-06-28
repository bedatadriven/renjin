package org.renjin.eval;

import org.renjin.sexp.StringVector;


/**
 * Provides implementations for the session-oriented R commands
 * like quit(), browse(), etc.
 *
 * <p>Proper implementations need to be provided by the host environment</p>
 */
public class SessionController {
  
  public enum SaveMode {
    NO,
    YES,
    ASK,
    DEFAULT
  }

  public void quit(Context context, SaveMode saveMode, int exitCode, boolean runLast ) {
    
  }
  
  public boolean isInteractive() {
    return false;
  }

  public int menu(StringVector choices) {
    throw new EvalException("menu() is not available");
  }
  
}
