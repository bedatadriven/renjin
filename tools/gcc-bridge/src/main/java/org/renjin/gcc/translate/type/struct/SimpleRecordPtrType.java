package org.renjin.gcc.translate.type.struct;

import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.SimpleRecordVar;
import org.renjin.gcc.translate.var.Variable;

public class SimpleRecordPtrType extends SimpleRecordType {

  public SimpleRecordPtrType(TranslationContext context, GimpleRecordType recordType) {
    super(context, recordType);
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new SimpleRecordVar(functionContext, gimpleName, this)
        .asPtrVariable();
  }
}
