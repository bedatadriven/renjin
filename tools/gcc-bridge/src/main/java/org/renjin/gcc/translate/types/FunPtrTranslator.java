package org.renjin.gcc.translate.types;


import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.FunPtrVar;
import org.renjin.gcc.translate.var.Variable;

public class FunPtrTranslator extends TypeTranslator {


  private TranslationContext context;
  private FunctionPointerType type;
  private String interfaceName;

  public FunPtrTranslator(TranslationContext context, FunctionPointerType type) {
    this.context = context;
    this.type = type;
    this.interfaceName = context.getFunctionPointerInterfaceName(type);
  }

  @Override
  public JimpleType returnType() {
    return jimpleType();
  }

  private JimpleType jimpleType() {
    return new JimpleType(interfaceName);
  }

  @Override
  public JimpleType paramType() {
    return jimpleType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new FunPtrVar(functionContext, gimpleName, type);
  }
}
