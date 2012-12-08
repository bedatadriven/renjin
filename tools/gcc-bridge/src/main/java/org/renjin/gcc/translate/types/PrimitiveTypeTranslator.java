package org.renjin.gcc.translate.types;


import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.PrimitiveHeapStorage;
import org.renjin.gcc.translate.var.PrimitiveStackStorage;
import org.renjin.gcc.translate.var.PrimitiveStorage;
import org.renjin.gcc.translate.var.PrimitiveVar;
import org.renjin.gcc.translate.var.Variable;

public class PrimitiveTypeTranslator extends TypeTranslator {

  private PrimitiveType type;

  public PrimitiveTypeTranslator(PrimitiveType type) {
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
    PrimitiveStorage storage;
    if(usage.isAddressed()) {
      storage = new PrimitiveHeapStorage(functionContext, type, gimpleName);
    } else {
      storage = new PrimitiveStackStorage(functionContext, type, gimpleName);
    }
    return new PrimitiveVar(functionContext, type, storage);
  }
}
