package org.renjin.repl;

public class QuitException extends RuntimeException {

  private int exitCode;

  public QuitException(int exitCode) {
    this.exitCode = exitCode;
  }

}
