package org.renjin.gcc.translate.types;

import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.struct.Struct;
import org.renjin.gcc.translate.var.StructPtrVar;
import org.renjin.gcc.translate.var.StructVar;
import org.renjin.gcc.translate.var.Variable;


public class StructTypeTranslator extends TypeTranslator {

  private GimpleStructType structType;
  private boolean pointer;
  private Struct struct;

  public StructTypeTranslator(TranslationContext translationContext, GimpleType type) {
    if(type instanceof PointerType) {
      this.structType = (GimpleStructType) ((PointerType) type).getInnerType();
      this.pointer = true;
    } else if(type instanceof GimpleStructType) {
      this.structType = (GimpleStructType) type;
      this.pointer = false;
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
    struct = translationContext.resolveStruct(structType.getName());

  }

  @Override
  public JimpleType paramType() {
    return struct.getJimpleType();
  }

  @Override
  public JimpleType returnType() {
    return new RealJimpleType(Object.class);
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    if(pointer) {
      return new StructPtrVar(functionContext, gimpleName, struct);
    } else {
      return new StructVar(functionContext, gimpleName, struct);
    }
  }
}
