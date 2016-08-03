package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;


public class ArrayExpr implements GExpr {
  
  private JExpr array;
  private JExpr offset;
  private ValueFunction valueFunction;
  private int length;
  
  public ArrayExpr(ValueFunction valueFunction, int length, JExpr array) {
    this.array = array;
    this.valueFunction = valueFunction;
    this.length = length;
    this.offset = Expressions.zero();
  }

  public ArrayExpr(ValueFunction valueFunction, int length, JExpr array, JExpr offset) {
    this.valueFunction = valueFunction;
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public JExpr getArray() {
    return array;
  }

  public JExpr getOffset() {
    return offset;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ArrayExpr rhsExpr = (ArrayExpr) rhs;
    int copyLength = Math.min(rhsExpr.length, length);
    
    valueFunction.memoryCopy(mv, 
        array, offset, 
        rhsExpr.getArray(), rhsExpr.getOffset(), 
        Expressions.constantInt(copyLength));
  }

  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
  }
}
