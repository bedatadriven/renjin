package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.types.PrimitiveTypes;

/**
 * Writes jimple instructions to store and retrieve a single primitive numeric
 * value on the JVM heap, by allocating a unit array. Variables stored this way
 * can be addressed and passed by reference to other methods.
 *
 */
public class NumericHeapStorage implements NumericStorage {
  private FunctionContext context;
  private String jimpleName;
  private PrimitiveType type;

  public NumericHeapStorage(FunctionContext context, PrimitiveType type, String gimpleName) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
    this.type = type;
    
    context.getBuilder().addVarDecl(PrimitiveTypes.getArrayType(type), jimpleName);
    context.getBuilder().addStatement(jimpleName + " = newarray (" + PrimitiveTypes.get(type) + ")[1]");
  }

  @Override
  public void assign(JimpleExpr expr) {
    context.getBuilder().addStatement(jimpleName + "[0] = " + expr);    
  }

  @Override
  public JimpleExpr asNumericExpr() {
    return new JimpleExpr(jimpleName + "[0]");
  }

  @Override
  public JimpleExpr addressOf() {
    JimpleType wrapperType = PrimitiveTypes.getWrapperType(type);
    String tempWrapper = context.declareTemp(wrapperType);
    context.getBuilder().addStatement(tempWrapper + " = new " + wrapperType);
    context.getBuilder().addStatement("specialinvoke " + tempWrapper + ".<" + wrapperType + ": void <init>(" + 
        PrimitiveTypes.getArrayType(type) + ")>(" + jimpleName + ")");
    
    return new JimpleExpr(tempWrapper);
  }

}
