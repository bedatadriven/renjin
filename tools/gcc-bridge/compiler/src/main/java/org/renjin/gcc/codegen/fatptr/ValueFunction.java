package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

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

  Optional<JExpr> getValueConstructor();


  GExpr dereference(JExpr array, JExpr offset);
  
  GExpr dereference(WrappedFatPtrExpr wrapperInstance);

  /**
   * Transforms the given expression to one or more array element values.
   * @param expr
   * @return
   */
  List<JExpr> toArrayValues(GExpr expr);

  /**
   * Copies the <strong>values</strong> from one array of these values to another.
   * 
   * @param valueCount The number of <strong>values</strong> to copy. Keep in mind that one
   *                   value may require several array elements. For example, a complex value occupies
   *                   two array elements.
   */
  void memoryCopy(MethodGenerator mv, 
                  JExpr destinationArray, JExpr destinationOffset, 
                  JExpr sourceArray, JExpr sourceOffset, JExpr valueCount);
  
}

