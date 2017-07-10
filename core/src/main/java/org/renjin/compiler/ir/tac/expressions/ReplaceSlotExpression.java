package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

import java.util.Map;

public class ReplaceSlotExpression implements Expression {
  
  private Expression object;
  private Expression value;
  private final Symbol name;
  
  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;
  
  public ReplaceSlotExpression(Expression object, Expression value, Symbol name) {
    this.object = object;
    this.value = value;
    this.name = name;
  }
  
  @Override
  public boolean isPure() {
    return true;
  }
  
  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException("TODO");
  }
  
  @Override
  public Type getType() {
    throw new UnsupportedOperationException("TODO");
  }
  
  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    throw new UnsupportedOperationException("TODO");
  }
  
  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }
  
  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      this.object = child;
    } else if(childIndex == 1) {
      this.value = child;
    } else {
      throw new IllegalArgumentException("childIndex: " + childIndex);
    }
  }
  
  @Override
  public int getChildCount() {
    return 2;
  }
  
  @Override
  public Expression childAt(int childIndex) {
    if(childIndex == 0) {
      return object;
    } else if(childIndex == 1) {
      return value;
    } else {
      throw new IllegalArgumentException("childIndex: " + childIndex);
    }
  }
  
  @Override
  public String toString() {
    return "replaceSlot(" + object + ", " + name + ", " + value + ")";
  }
}
