package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.ssa.VariableMap;

/**
 * Checks whether op1 is greater than or equal to op2. 
 * Op1 and op2 must be integers. (Not sexps!)
 */
public class CmpGE extends SpecializedCallExpression {

  public CmpGE(Expression op1, Expression op2) {
    super(op1, op2);
  }

  @Override
  public String toString() {
    return arguments[0] + " >= " + arguments[1];
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class getType() {
    return boolean.class;
  }

  @Override
  public Class resolveType(VariableMap variableMap) {
    return boolean.class;
  }
}
