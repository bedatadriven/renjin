package org.renjin.gcc.translate.type.struct;

import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.var.SimpleRecordVar;
import org.renjin.gcc.translate.var.Variable;

public class SimpleRecordPtrType implements ImType {

  private final SimpleRecordType baseType;

  public SimpleRecordPtrType(TranslationContext context, SimpleRecordType recordType) {
    this.baseType = recordType;
  }

  @Override
  public JimpleType paramType() {
    return baseType.getJimpleType();
  }

  @Override
  public JimpleType returnType() {
    return baseType.getJimpleType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new SimpleRecordVar(functionContext, gimpleName, baseType)
        .asPtrVariable();
  }

  @Override
  public ImType pointerType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException();
  }
}
