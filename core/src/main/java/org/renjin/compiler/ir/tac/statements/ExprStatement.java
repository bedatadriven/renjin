package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Collections;


/**
 * Statement that is evaluated for side-effects
 */
public class ExprStatement implements Statement {

  private Expression operand;
  
  public ExprStatement(Expression operand) {
    super();
    this.operand = operand;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  
  @Override
  public Expression getRHS() {
    return operand;
  }

  @Override
  public String toString() {
    return operand.toString();
  }

  @Override
  public void setRHS(Expression newRHS) {
    this.operand = newRHS;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return operand;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      operand = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitExprStatement(this);
  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {
    if(!operand.isDefinitelyPure()) {
      int stackSizeIncrease = operand.load(emitContext, mv);
      mv.visitInsn(Opcodes.POP);
      return stackSizeIncrease;
    } else {
      return 0;
    }
  }
}
