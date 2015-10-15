package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;

/**
 * A transformer which updates the gimple function body prior to translation
 */
public interface FunctionBodyTransformer {

  /**
   * Applies a transformation to the body of the function.
   * @param unit
   * @param fn
   * @return true if the body was updated
   */
  boolean transform(GimpleCompilationUnit unit, GimpleFunction fn);


}
