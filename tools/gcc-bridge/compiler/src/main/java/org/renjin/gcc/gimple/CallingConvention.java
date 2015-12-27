package org.renjin.gcc.gimple;

/**
 * Defines various aspects of function calling conventions for
 * different languages supported by GCC.
 */
public class CallingConvention {

  /**
   *
   *
   * @param declaredFunctionName the name of the function as declared in the source
   * @return the "mangled" name which will be written into assembly and by which other
   * code will reference it.
   */
  public String mangleFunctionName(String declaredFunctionName) {
    return declaredFunctionName;
  }

}
