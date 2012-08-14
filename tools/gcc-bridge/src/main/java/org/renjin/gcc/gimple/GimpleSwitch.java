package org.renjin.gcc.gimple;


public class GimpleSwitch extends GimpleIns {
  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitSwitch(this);
  }
}
