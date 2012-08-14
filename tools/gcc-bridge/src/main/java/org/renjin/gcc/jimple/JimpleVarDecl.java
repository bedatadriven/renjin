package org.renjin.gcc.jimple;

public class JimpleVarDecl {
  private JimpleType type;
  private String name;

  public JimpleVarDecl(JimpleType type, String name) {
    this.type = type;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public JimpleType getType() {
    return type;
  }

  @Override
  public String toString() {
    return type + " " + name;
  }
}
