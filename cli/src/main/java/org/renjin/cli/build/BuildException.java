package org.renjin.cli.build;


public class BuildException extends RuntimeException {

  public BuildException(String message) {
    super(message);
  }

  public BuildException(String message, Exception cause) {
    super(message, cause);
  }
}
