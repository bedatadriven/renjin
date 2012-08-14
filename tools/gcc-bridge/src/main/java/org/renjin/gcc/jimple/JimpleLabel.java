package org.renjin.gcc.jimple;


public class JimpleLabel extends JimpleBodyElement {
  private final String name;

  public JimpleLabel(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name + ":";
  }
}
