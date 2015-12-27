package org.renjin.gcc.gimple;

public class F77CallingConvention extends CallingConvention {

  @Override
  public String mangleFunctionName(String declaredFunctionName) {
    // See http://en.wikipedia.org/wiki/Name_mangling#Name_mangling_in_Fortran
    if (declaredFunctionName.contains("_")) {
      return declaredFunctionName.toLowerCase() + "__";
    } else {
      return declaredFunctionName.toLowerCase() + "_";
    }
  }

}
