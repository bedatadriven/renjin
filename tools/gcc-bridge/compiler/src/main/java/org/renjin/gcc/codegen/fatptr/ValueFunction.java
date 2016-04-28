package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import java.util.List;

/**
 * Functor which can "unwrap" a fat ptr
 */
public interface ValueFunction {
  
  Type getValueType();

  /**
   * Returns the number of array elements required for each value.
   * 
   * <p>For an array of doubles, for example, the length is 1. By contrast, a complex number value requires
   * two array elements per value.</p>
   */
  int getElementLength();

  /**
   * Returns the size of the value in bytes, as understood by GCC/Gimple. 
   * 
   */
  int getElementSize();

  Optional<SimpleExpr> getValueConstructor();


  Expr dereference(SimpleExpr array, SimpleExpr offset);

  /**
   * Transforms the given expression to one or more array element values.
   * @param expr
   * @return
   */
  List<SimpleExpr> toArrayValues(Expr expr);

}

