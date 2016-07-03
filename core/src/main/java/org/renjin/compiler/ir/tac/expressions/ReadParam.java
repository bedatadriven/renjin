package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.InlineParamExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

import java.util.Map;


public class ReadParam implements Expression {

  private final Symbol param;
  private ValueBounds valueBounds;
  private Type type;

  public ReadParam(Symbol param) {
    this.param = param;
    this.valueBounds = ValueBounds.UNBOUNDED;
    this.type = valueBounds.storageType();
  }

  public Symbol getParam() {
    return param;
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
    return "param(" + param + ")";
  }
}
