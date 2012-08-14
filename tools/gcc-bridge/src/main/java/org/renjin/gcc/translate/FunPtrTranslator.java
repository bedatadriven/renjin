package org.renjin.gcc.translate;


import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;

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
  public void declareParameter(JimpleMethodBuilder builder, GimpleParameter param) {
    builder.addParameter(jimpleType(), param.getName());
  }

  @Override
  public JimpleType returnType() {
    return jimpleType();
  }

  private JimpleType jimpleType() {
    return new JimpleType(interfaceName);
  }

  @Override
  public void declareVariable(JimpleMethodBuilder builder, GimpleVarDecl varDecl) {
    builder.addVarDecl(jimpleType(), builder.resolveVarName(varDecl.getName()));
  }

  @Override
  public JimpleType paramType() {
    return jimpleType();
  }
}
