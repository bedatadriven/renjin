package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.expr.SimpleExpr;

public interface MethodHandleGenerator {
  
  SimpleExpr getMethodHandle();
}
