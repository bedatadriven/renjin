package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.InlineParamExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Map;


public class ReadParam implements Expression {

  private final Symbol param;
  private final SEXP defaultValue;
  private ValueBounds valueBounds;
  private Type type;

  public ReadParam(Symbol param, SEXP defaultValue) {
    this.param = param;
    this.defaultValue = defaultValue;
    this.valueBounds = ValueBounds.of(defaultValue);
    this.type = valueBounds.storageType();
  }

  public Symbol getParam() {
    return param;
  }

  public SEXP getDefaultValue() {
    return defaultValue;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    InlineParamExpr paramaterValue = emitContext.getInlineParameter(param);
    paramaterValue.load(mv);
    return 0;
  }

  @Override
  public Type getType() {
    return type.getReturnType();
  }

  public void updateBounds(ValueBounds argumentBounds) {
    valueBounds = argumentBounds;
    type = argumentBounds.storageType();
  }
  
  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException("no children");
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException("no children");
  }

  @Override
  public String toString() {
    if(defaultValue == Symbol.MISSING_ARG) {
      return "param(" + param + ")";
    } else {
      return "param(" + param + " = " + defaultValue + ")";
    }
  }
}
