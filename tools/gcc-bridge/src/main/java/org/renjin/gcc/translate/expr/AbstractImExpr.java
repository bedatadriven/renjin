package org.renjin.gcc.translate.expr;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.PrimitiveType;

/**
 * Provides default implementations for the {@link ImExpr} interface
 */
public abstract class AbstractImExpr implements ImExpr {
  
  @Override
  public ImExpr addressOf() {
    throw new UnsupportedOperationException(this + " is not addressable");
  }

  @Override
  public ImExpr memref() {
    throw new UnsupportedOperationException("Expression " + this + " (" + getClass().getSimpleName() + ") does not" +
            " support dereferencing");
  }

  @Override
  public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
    throw new UnsupportedOperationException("Expression " + this + " (" + getClass().getSimpleName() + ") cannot " +
            "be expressed as a primitive");
  }

  @Override
  public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
    throw new UnsupportedOperationException("Expression " + this + " (" + getClass().getSimpleName() + ") cannot " +
        "be expressed as an instance of " + className);
  }

  @Override
  public ImExpr elementAt(ImExpr index) {
    throw new UnsupportedOperationException(this + " (" + getClass().getSimpleName() + ") is not an array");
  }

  @Override
  public ImExpr pointerPlus(ImExpr offset) {
    throw new UnsupportedOperationException("Expression " + this + "  does not support pointer arithmatic");
  }

  @Override
  public ImExpr member(String member) {
    throw new UnsupportedOperationException("Expression " + this + "  does not support members");
  }

  @Override
  public boolean isNull() {
    return false;
  }
}
