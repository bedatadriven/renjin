package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.var.PrimitiveVarGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.Collections;
import java.util.List;

/**
 * Parameter that is a simple primitive (e.g. double, int, etc)
 */
public class PrimitiveParamGenerator extends ParamGenerator {

  private final GimpleParameter param;
  private GimplePrimitiveType type;
  private int localVariableIndex;

  public PrimitiveParamGenerator(GimpleParameter param, int localVariableIndex) {
    this.param = param;
    this.localVariableIndex = localVariableIndex;
    this.type = (GimplePrimitiveType) param.getType();
  }

  public GimplePrimitiveType getType() {
    return type;
  }

  @Override
  public int getGimpleId() {
    return param.getId();
  }

  @Override
  public int numSlots() {
    return type.localVariableSlots();
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type.jvmType());
  }

  @Override
  public VarGenerator emitInitialization(MethodVisitor methodVisitor, LocalVarAllocator localVars) {
    // No initialization required, already set to the local variable
    return new PrimitiveVarGenerator(localVariableIndex, param.getName(), type);
  }
}
