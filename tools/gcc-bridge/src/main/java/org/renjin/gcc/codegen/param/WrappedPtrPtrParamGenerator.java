package org.renjin.gcc.codegen.param;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.runtime.Ptr;

import java.util.Collections;
import java.util.List;

public class WrappedPtrPtrParamGenerator extends ParamGenerator {

  private GimpleParameter parameter;
  private int localVariableIndex;
  private final GimpleIndirectType type;

  /**
   * The {@link Ptr} subclass type
   */
  private final WrapperType pointerType;

  public WrappedPtrPtrParamGenerator(GimpleParameter parameter, int localVariableIndex) {
    this.parameter = parameter;
    this.localVariableIndex = localVariableIndex;
    this.type = (GimpleIndirectType) parameter.getType();
    this.pointerType = WrapperType.forPointerType((GimpleIndirectType) type.getBaseType());
  }

  @Override
  public int getGimpleId() {
    return parameter.getId();
  }

  @Override
  public int numSlots() {
    return 1;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(pointerType.getWrapperType());
  }

  @Override
  public VarGenerator emitInitialization(MethodVisitor mv, LocalVarAllocator localVars) {
    return new PtrPtrParamVarGenerator(parameter.getType(), pointerType, localVariableIndex);
  }
}
