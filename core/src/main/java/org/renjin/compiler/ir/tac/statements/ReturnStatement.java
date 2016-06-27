package org.renjin.compiler.ir.tac.statements;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;

import java.util.Collections;


public class ReturnStatement implements Statement, BasicBlockEndingStatement {

  private Expression value;

  public ReturnStatement(Expression value) {
    super();
    this.value = value;
  }
  
  public Expression getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return "return " + value;
  }

  @Override
  public Expression getRHS() {
    return value;
  }
  
  @Override
  public void setRHS(Expression newRHS) {
    this.value = newRHS;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return value;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      value = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitReturn(this);
  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {
    mv.areturn(Type.VOID_TYPE);
    return 0;
  }
}
