package org.renjin.repl;

import org.renjin.primitives.special.ControlFlowException;

public class QuitException extends ControlFlowException {

  private int exitCode;

  public QuitException(int exitCode) {
    this.exitCode = exitCode;
  }

}
