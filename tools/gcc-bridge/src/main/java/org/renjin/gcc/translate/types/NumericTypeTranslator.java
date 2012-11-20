package org.renjin.gcc.translate.types;


import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.NumericHeapStorage;
import org.renjin.gcc.translate.var.NumericStackStorage;
import org.renjin.gcc.translate.var.NumericStorage;
import org.renjin.gcc.translate.var.NumericVar;
import org.renjin.gcc.translate.var.Variable;

public class NumericTypeTranslator extends TypeTranslator {

  private PrimitiveType type;

  public NumericTypeTranslator(PrimitiveType type) {
    this.type = type;
  }

  @Override
  public JimpleType paramType() {
    return asJimple();
  }

  @Override
  public JimpleType returnType() {
    return PrimitiveTypes.get(type);
  }

  private JimpleType asJimple() {
    return PrimitiveTypes.get(type);
  }


  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    NumericStorage storage;
    if(usage.isAddressed()) {
      storage = new NumericHeapStorage(functionContext, type, gimpleName);
    } else {
      storage = new NumericStackStorage(functionContext, type, gimpleName);
    }
    return new NumericVar(functionContext, type, storage);
  }
}
