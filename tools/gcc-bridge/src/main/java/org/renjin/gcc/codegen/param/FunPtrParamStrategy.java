package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.FunPtrVarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;

/**
 * Strategy for function pointer parameters implemented using a {@link MethodHandle} parameter.
 */
public class FunPtrParamStrategy extends ParamStrategy {

  private GimpleType parameterType;

  public FunPtrParamStrategy(GimpleType parameterType) {
    this.parameterType = parameterType;
  }
  
  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(Type.getType(MethodHandle.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, int localVarIndex, LocalVarAllocator localVars) {
    return new FunPtrVarGenerator((GimpleFunctionType) parameterType.getBaseType(), localVarIndex);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator generator) {
    generator.emitPushMethodHandle(mv);
  }
}
