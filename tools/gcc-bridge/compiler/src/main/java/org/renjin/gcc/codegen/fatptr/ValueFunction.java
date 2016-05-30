package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.repackaged.guava.base.Optional;

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
   * Returns the size of each array value in bytes. This value is used to convert
   * offsets in bytes to offsets in number of elements. 
   */
  int getArrayElementBytes();

  Optional<SimpleExpr> getValueConstructor();


  Expr dereference(SimpleExpr array, SimpleExpr offset);

  /**
   * Transforms the given expression to one or more array element values.
   * @param expr
   * @return
   */
  List<SimpleExpr> toArrayValues(Expr expr);

}

