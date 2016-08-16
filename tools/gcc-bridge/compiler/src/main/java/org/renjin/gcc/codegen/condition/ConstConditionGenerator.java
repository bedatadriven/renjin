package org.renjin.gcc.codegen.condition;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Label;


public class ConstConditionGenerator implements ConditionGenerator {
  private boolean value;

  public ConstConditionGenerator(boolean value) {
    this.value = value;
  }


  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    if(value) {
      mv.goTo(trueLabel);
    } else {
      mv.goTo(falseLabel);
    }
  }
}
