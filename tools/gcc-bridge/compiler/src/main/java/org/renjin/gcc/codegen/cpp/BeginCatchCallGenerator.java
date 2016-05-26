package org.renjin.gcc.codegen.cpp;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.statement.GimpleCall;


public class BeginCatchCallGenerator implements CallGenerator {
  
  public static final String NAME = "__cxa_begin_catch";
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    // NOOP
  }
}
