package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;

/**
 * Strategy for function pointer parameters implemented using a {@link MethodHandle} parameter.
 */
public class FunPtrParamStrategy implements ParamStrategy {

  private GimpleType parameterType;

  public FunPtrParamStrategy(GimpleType parameterType) {
    this.parameterType = parameterType;
  }
  
  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(Type.getType(MethodHandle.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return new FunPtrVarGenerator((GimpleFunctionType) parameterType.getBaseType(), paramVars.get(0));
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator generator) {
    generator.emitPushMethodHandle(mv);
  }
}
