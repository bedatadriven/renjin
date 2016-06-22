package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeStrategy;

/**
 * Type strategy for arrays of values
 */
public interface ArrayTypeStrategy<ExprT extends GExpr> extends TypeStrategy<ExprT> {

  GExpr elementAt(GExpr array, GExpr index);
  
  
}
