package org.renjin.gcc.translate.types;

import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.MappedStructPtrVar;
import org.renjin.gcc.translate.var.Variable;


public class StructTypeTranslator extends TypeTranslator {

  private GimpleStructType structType;

  public StructTypeTranslator(GimpleStructType structType) {
    this.structType = structType;
  }

  @Override
  public JimpleType paramType() {
    return new JimpleType(Object.class);
  }

  @Override
  public JimpleType returnType() {
    return new JimpleType(Object.class);
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName) {
    return new MappedStructPtrVar(structType);
  }
}
