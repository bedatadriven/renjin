package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.runtime.Pointer;


public class PointerTypeTranslator extends TypeTranslator {

  public PointerTypeTranslator(PointerType type) {
    super();
  }

  @Override
  public void declareParameter(JimpleMethodBuilder builder, GimpleParameter param) {
    builder.addParameter(new JimpleType(Pointer.class), param.getName());
  }

  @Override
  public JimpleType returnType() {
    return new JimpleType(Pointer.class);
  }

  @Override
  public JimpleType paramType() {
    return new JimpleType(Pointer.class);
  }

  @Override
  public void declareVariable(JimpleMethodBuilder builder, GimpleVarDecl varDecl) {
    builder.addVarDecl(new JimpleType(Pointer.class), builder.resolveVarName(varDecl.getName()));
  }
}
