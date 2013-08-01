package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.eval.Context;
import org.renjin.sexp.Symbol;



/**
 * A value known at compile time.
 */
public class Constant implements SimpleExpression {

  private Object value;
  
  public Constant(Object value) {
    this.value = value;
  }
  
  public Object getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    if(value instanceof Symbol) {
      return "|" + value + "|";
    } else {
      return value.toString();
    }
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class inferType() {
    return value.getClass();
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }
}
