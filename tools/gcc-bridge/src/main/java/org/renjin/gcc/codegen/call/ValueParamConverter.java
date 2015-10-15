package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.PrimitiveGenerator;

/**
 * Generates the code to push a parameter as is 
 */
public class ValueParamConverter implements ParamConverter {
  
  private PrimitiveGenerator valueGenerator;

  public ValueParamConverter(PrimitiveGenerator valueGenerator) {
    this.valueGenerator = valueGenerator;
  }

  @Override
  public void emitPushParam(MethodVisitor mv) {
    valueGenerator.emitPush(mv);
  }
}
