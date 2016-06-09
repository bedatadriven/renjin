package org.renjin.gcc;

/**
 * TreeLogger implementation which does nothing.
 */
public class NullTreeLogger extends TreeLogger {
  @Override
  public void log(Level level, String message) {
  }

  @Override
  public TreeLogger branch(Level level, String message) {
    return this;
  }

  @Override
  public TreeLogger debug(String message, Object code) {
    return this;
  }

  @Override
  public void finish() {
  }
}
