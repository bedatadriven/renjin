package org.renjin.gcc.codegen.cpp;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.statement.GimpleCall;

/**
 * Generates a call to __builtin_eh_pointer, which ignore
 */
public class EhPointerCallGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_eh_pointer";


  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    // NOOP
  }
}
