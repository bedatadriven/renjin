package org.renjin.packaging;

/**
 * Logs messages during the build
 */
public interface BuildLogger {
  void info(String message);
  void debug(String message);
}
