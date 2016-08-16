package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Label;

/**
 * Marker interface for pointer expressions
 */
public interface PtrExpr extends GExpr {
  
  void jumpIfNull(MethodGenerator mv, Label label);
  
  GExpr valueOf();
}
