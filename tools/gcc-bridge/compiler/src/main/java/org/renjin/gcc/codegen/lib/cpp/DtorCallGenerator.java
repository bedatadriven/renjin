package org.renjin.gcc.codegen.lib.cpp;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;

public class DtorCallGenerator implements CallGenerator {

  /**
   * Constructors and destructors basically works the same way.
   * Source:
   * http://d3s.mff.cuni.cz/software/gmc/download/Thesis-JanSebetovsky-C++Support.pdf#chapter.4
   * __comp_dtor should call a "complete destructor" if it exists. Calls "base destructor" otherwise.
   * Source:
   * http://d3s.mff.cuni.cz/software/gmc/download/Thesis-JanSebetovsky-C++Support.pdf#section.4.1
   */
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    GimpleFunctionRef baseConstructor = new GimpleFunctionRef();
    baseConstructor.setName("__base_dtor ");
    GimpleAddressOf functionExpr = new GimpleAddressOf();
    functionExpr.setValue(baseConstructor);
    CallGenerator baseCtorCallGenerator = exprFactory.findCallGenerator(functionExpr, call.getOperands());
    baseCtorCallGenerator.emitCall(mv, exprFactory, call);
  }
}