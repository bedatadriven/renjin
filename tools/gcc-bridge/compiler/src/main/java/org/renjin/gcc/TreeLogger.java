package org.renjin.gcc;

import java.io.IOException;

/**
 * Supports detailed logging of the compilation process
 */
public abstract class TreeLogger {


  public enum Level {
    INFO,
    DEBUG
  }
  
  
  public final void info(String message) {
    log(Level.INFO, message);
  }

  public final TreeLogger branch(String message) {
    return branch(Level.INFO, message);
  }
  
  public final void debug(String message) {
    log(Level.DEBUG, message);
  }

  public abstract void log(Level level, String message);

  public abstract TreeLogger branch(Level level, String message);
  
  public abstract TreeLogger debug(String message, Object code);

  public abstract void finish() throws IOException;

  
}
