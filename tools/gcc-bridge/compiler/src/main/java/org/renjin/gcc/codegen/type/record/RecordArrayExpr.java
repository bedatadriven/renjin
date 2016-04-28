package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.Addressable;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;

/**
 * Record value expression, backed by a JVM primitive array 
 */
public interface RecordArrayExpr extends LValue, Addressable {

  /**
   * @return the array backing this record value
   */
  SimpleExpr getArray();

  /**
   * 
   * @return the offset (in elements) within this array at which the record value starts.
   */
  SimpleExpr getOffset();


  /**
   * @return an array that can be returned as the result of a method call.
   */
  SimpleExpr arrayForReturning();

}
