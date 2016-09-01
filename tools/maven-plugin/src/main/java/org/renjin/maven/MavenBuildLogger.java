package org.renjin.maven;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.renjin.packaging.BuildLogger;

public class MavenBuildLogger implements BuildLogger {
  
  private Log log;

  public MavenBuildLogger() {
    log = new SystemStreamLog();
  }

  public Log getLog() {
    return log;
  }

  @Override
  public void info(String message) {
    log.info(message);
  }

  @Override
  public void debug(String message) {
    log.debug(message);
  }

  @Override
  public void error(String message) {
    log.error(message);
  }
}
