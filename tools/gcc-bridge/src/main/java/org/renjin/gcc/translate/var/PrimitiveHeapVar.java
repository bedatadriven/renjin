package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.PrimitiveAssignment;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * Writes jimple instructions to store and retrieve a single primitive numeric
 * value on the JVM heap, by allocating a unit array. Variables stored this way
 * can be addressed and passed by reference to other methods.
 * 
 */
public class PrimitiveHeapVar extends AbstractImExpr implements Variable, PrimitiveLValue, ImLValue {
  private FunctionContext context;
  private String jimpleName;
  private ImPrimitiveType type;

  public PrimitiveHeapVar(FunctionContext context, ImPrimitiveType type, String gimpleName) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
    this.type = type;

    context.getBuilder().addVarDecl(type.getArrayClass(), jimpleName);
    context.getBuilder().addStatement(jimpleName + " = newarray (" + type.asJimple() + ")[1]");
  }

  @Override
  public void writePrimitiveAssignment(JimpleExpr expr) {
    context.getBuilder().addStatement(jimpleName + "[0] = " + expr);
  }

  @Override
  public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
    return type.castIfNeeded(jimple(), this.type);
  }

  public JimpleExpr jimple() {
    return new JimpleExpr(jimpleName + "[0]");
  }


  @Override
  public ImPrimitiveType type() {
    return type;
  }

  @Override
  public String toString() {
    return "heap:" + jimpleName;
  }

  @Override
  public ImExpr addressOf() {
    return new PointerTo();
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    PrimitiveAssignment.assign(context, this, rhs);
  }

  private class PointerTo extends AbstractImExpr implements ImIndirectExpr {

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      return new ArrayRef(jimpleName, 0);
    }

    @Override
    public ImPrimitivePtrType type() {
      return PrimitiveHeapVar.this.type().pointerType();
    }

    @Override
    public ImExpr memref() {
      return PrimitiveHeapVar.this;
    }

    @Override
    public String toString() {
      return "&" + PrimitiveHeapVar.this.toString();
    }
  }
}
