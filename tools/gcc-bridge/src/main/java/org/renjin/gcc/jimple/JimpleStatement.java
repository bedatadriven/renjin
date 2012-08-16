package org.renjin.gcc.jimple;


public class JimpleStatement extends JimpleBodyElement {

  private String text;

  public JimpleStatement(String text) {
    this.text = text;
  }

  @Override
  public void write(JimpleWriter w) {
    w.println(toString());
  }

  @Override
  public String toString() {
    return text + ";";
  }
}
