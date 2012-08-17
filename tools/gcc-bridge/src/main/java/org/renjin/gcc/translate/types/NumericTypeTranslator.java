package org.renjin.gcc.translate.types;


import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
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
    return new JimpleType(asJimpleName());
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

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName) {


    return new NumericVar(functionContext, gimpleName, type);
  }
}
