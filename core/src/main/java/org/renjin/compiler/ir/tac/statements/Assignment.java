package org.renjin.compiler.ir.tac.statements;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.VariableStorage;
import org.renjin.compiler.ir.IRFormatting;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;

import java.util.Collections;


public class Assignment implements Statement {
  private LValue lhs;
  private Expression rhs;
 
  public Assignment(LValue lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
 
  public LValue getLHS() {
    return lhs;
  }
 
  @Override
  public Expression getRHS() {
    return rhs;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  

  @Override
  public void setRHS(Expression newRHS) {
    this.rhs = newRHS;
  }

  @Override
  public String toString() {
    return getLHS() + " " + IRFormatting.LEFT_ARROW + " "  + rhs;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return rhs;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      rhs = child;
    } else {
      throw new IllegalArgumentException("childIndex=" + childIndex);
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitAssignment(this);
  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {

    VariableStorage storage = emitContext.getVariableStorage(lhs);
    Type rhsType;
    if(rhs instanceof LValue) {
      rhsType = emitContext.getVariableStorage((LValue) rhs).getType();
    } else {
      rhsType = rhs.getType();
    }
    
    int stackIncrease = rhs.load(emitContext, mv);
    emitContext.convert(mv, rhsType, storage.getType());
    mv.visitVarInsn(storage.getType().getOpcode(Opcodes.ISTORE), storage.getSlotIndex());
    return stackIncrease;
  }


  public void setLHS(LValue lhs) {
    this.lhs = lhs;
  }
}
