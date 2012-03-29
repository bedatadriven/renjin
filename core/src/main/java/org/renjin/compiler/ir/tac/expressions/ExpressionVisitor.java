package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.ssa.SsaVariable;

public interface ExpressionVisitor {

  void visitConstant(Constant constant);
  void visitDynamicCall(DynamicCall call);
  void visitElementAccess(ElementAccess expr);
  void visitEnvironmentVariable(EnvironmentVariable variable);
  void visitIncrement(Increment increment);
  void visitLocalVariable(LocalVariable variable);
  void visitMakeClosure(MakeClosure closure);
  void visitPrimitiveCall(PrimitiveCall call);
  void visitTemp(Temp temp);
  void visitCmpGE(CmpGE cmp);
  void visitSsaVariable(SsaVariable variable);
  void visitPhiFunction(PhiFunction phiFunction);
  void visitPromise(IRThunk promise);
  void visitLength(Length length);
}
