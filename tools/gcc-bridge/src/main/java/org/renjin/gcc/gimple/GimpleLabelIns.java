package org.renjin.gcc.gimple;


public class GimpleLabelIns extends GimpleIns {

  private final GimpleLabel label;

  public GimpleLabelIns(GimpleLabel label) {
    this.label = label;
  }

  public GimpleLabel getLabel() {
    return label;
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitLabelIns(this);
  }
}
