package org.renjin.packaging;

class SimpleLogger implements BuildLogger {
  @Override
  public void info(String message) {
    System.err.println("[INFO] " + message);
  }

  @Override
  public void debug(String message) {
    System.err.println("[DEBUG] " + message);
  }

  @Override
  public void error(String message) {
    System.err.println("[ERROR] " + message);
  }

  @Override
  public void error(String message, Exception e) {
    System.err.println("[ERR] " + message);
    e.printStackTrace(System.err);
  }
}
