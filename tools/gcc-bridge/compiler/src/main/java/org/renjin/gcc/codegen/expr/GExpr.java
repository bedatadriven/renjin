package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;

/**
 * Interface for generators which can emit load/store operations for {@code GimpleExpr}s
 * 
 * <p>{@code Expr}s can either be simple, meaning they are represented by a single JVM value, or 
 * composite expressions, like {@link org.renjin.gcc.codegen.type.complex.ComplexValue} or 
 * {@link FatPtrPair} which are represented with multiple JVM values.</p>
 */
public interface GExpr {
  
  void store(MethodGenerator mv, GExpr rhs);
  
  GExpr addressOf();
  
}
