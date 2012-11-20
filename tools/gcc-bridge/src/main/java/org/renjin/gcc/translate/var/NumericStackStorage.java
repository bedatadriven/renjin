package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.types.PrimitiveTypes;

/**
 * Writes jimple instructions to store and retrieve a single primitive numeric
 * value in a local JVM variable, allocated on the stack. 
 */
public class NumericStackStorage implements NumericStorage {

  private FunctionContext context;
  private String jimpleName;

  public NumericStackStorage(FunctionContext context, PrimitiveType type, String gimpleName) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
    
    context.getBuilder().addVarDecl(PrimitiveTypes.get(type), jimpleName);
  }

  @Override
  public void assign(JimpleExpr expr) {
    context.getBuilder().addStatement(jimpleName + " = " + expr);    
  }

  @Override
  public JimpleExpr asNumericExpr() {
    return new JimpleExpr(jimpleName);
  }

  @Override
  public JimpleExpr addressOf() {
    throw new UnsupportedOperationException(jimpleName + " is not addressable, stored on stack");
  }
}
