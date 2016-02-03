package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.var.Value;

import java.util.List;

/**
 * Generates the code to invoke a specific call
 */
public interface CallInvoker {
  
  List<ParamStrategy> getParamStrategy();
  
  
  ReturnStrategy getReturnStrategy();
  
  
}
