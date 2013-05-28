package org.renjin.gcc.jimple;

public class JimpleGoto extends JimpleStatement {

  public JimpleGoto(String target) {
    super("goto " + target);
  }
}
