package org.renjin.gcc.translate;


import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;

public class PrimitiveTypeTranslator extends TypeTranslator {

  private PrimitiveType type;

  public PrimitiveTypeTranslator(PrimitiveType type) {
    this.type = type;
  }

  @Override
  public void declareParameter(JimpleMethodBuilder builder, GimpleParameter param) {
    builder.addParameter(asJimple(), param.getName());
  }

  @Override
  public JimpleType paramType() {
    return asJimple();
  }

  @Override
  public JimpleType returnType() {
    return new JimpleType(asJimpleName());
  }

  @Override
  public void declareVariable(JimpleMethodBuilder builder, GimpleVarDecl varDecl) {
    builder.addVarDecl(asJimple(), builder.resolveVarName(varDecl.getName()));
  }

  private JimpleType asJimple() {
    return new JimpleType(asJimpleName());
  }

  private String asJimpleName() {
    switch(type) {
      case DOUBLE_TYPE:
        return "double";
      case VOID_TYPE:
        return "void";
      case INT_TYPE :
        return "int";
      case BOOLEAN:
        return "boolean";
    }
    throw new UnsupportedOperationException(type.name());
  }
}
