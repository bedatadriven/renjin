package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.Closure;
import org.renjin.sexp.SEXP;

import java.util.List;
import java.util.Map;


public class ClosureCall implements Expression {

  private String name;
  private Closure closure;
  private List<Expression> arguments;

  public ClosureCall(String name, Closure closure, List<Expression> arguments) {
    this.name = name;
    this.closure = closure;
    this.arguments = arguments;
  }

  @Override
  public boolean isDefinitelyPure() {
    return false;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getType() {
    return Type.getType(SEXP.class);
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex, child);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    return arguments.get(index);
  }

  @Override
  public String toString() {
    return name + "(" + Joiner.on(", ").join(arguments) + ")";
  }
}
