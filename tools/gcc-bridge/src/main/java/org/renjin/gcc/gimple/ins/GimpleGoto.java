package org.renjin.gcc.gimple.ins;

import org.renjin.gcc.gimple.GimpleLabel;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.ins.GimpleIns;

import java.util.Collections;
import java.util.Set;

public class GimpleGoto extends GimpleIns {
  private int target;

  public GimpleLabel getTargetLabel() {
    return new GimpleLabel("bb" + target);
  }

  public int getTarget() {
    return target;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "goto <" + getTargetLabel() + ">";
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitGoto(this);

  }

  @Override
  public Set<Integer> getJumpTargets() {
    return Collections.singleton(target);
  }
}
