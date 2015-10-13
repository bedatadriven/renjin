package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.var.VarGenerator;

import java.util.List;

public abstract class ParamGenerator {

  
  public abstract int getGimpleId();
  
  /**
   * @return number of local variable slots occupied by this parameter
   */
  public abstract int numSlots();

  /**
   * 
   * @return one or more parameter types to which this parameter maps
   */
  public abstract List<Type> getParameterTypes();
  
  public abstract VarGenerator emitInitialization(MethodVisitor methodVisitor, LocalVarAllocator localVars);
  
}
