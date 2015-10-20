package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ValueGenerator;

/**
 * Generates the code to push a parameter as is 
 */
public class ValueParamConverter implements ParamConverter {
  
  private ValueGenerator valueGenerator;

  public ValueParamConverter(ValueGenerator valueGenerator) {
    this.valueGenerator = valueGenerator;
  }

  @Override
  public void emitPushParam(MethodVisitor mv) {
    valueGenerator.emitPushValue(mv);
  }
}
