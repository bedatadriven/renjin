package org.renjin.gcc.codegen.cpp;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.statement.GimpleCall;


/**
 * will check if this object was already initialized or if anyone else is trying to initialize this object, 
 * and then it will signal the calling method to run x’s constructor if it’s safe to do so.
 */
public class GuardAcquireGenerator implements CallGenerator {
  
  public static final String NAME = "__cxa_guard_acquire";
  

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    mv.iconst(1); 
  }
}
