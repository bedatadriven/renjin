package org.renjin.gcc.codegen.lib.cpp;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;

public class CtorCallGenerator implements CallGenerator {

  /**
   * __comp_ctor should call a "complete constructor" if it exists. Calls "base constructor" otherwise.
   * Source:
   * http://d3s.mff.cuni.cz/software/gmc/download/Thesis-JanSebetovsky-C++Support.pdf#section.4.1
   */
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    GimpleFunctionRef baseConstructor = new GimpleFunctionRef();
    baseConstructor.setName("__base_ctor ");
    GimpleAddressOf functionExpr = new GimpleAddressOf();
    functionExpr.setValue(baseConstructor);
    CallGenerator baseCtorCallGenerator = exprFactory.findCallGenerator(functionExpr);
    baseCtorCallGenerator.emitCall(mv, exprFactory, call);
  }
}