package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy for parameters of type {@code complex *} that uses two arguments: a {@code double} or {@code float}
 * array and an integer offset parameter.
 */
public class ComplexPtrParamStrategy implements ParamStrategy {
  
  private GimpleType type;
  private GimpleComplexType baseType;
  
  public ComplexPtrParamStrategy(GimpleType type) {
    this.type = type;
    this.baseType = type.getBaseType();
  }

  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(baseType.getJvmPartArrayType(), Type.getType(int.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return new ComplexPtrVarGenerator(type, paramVars.get(0), paramVars.get(1));
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator parameterValueGenerator) {
    // Need to push both the double[] array
    // and the int offset
    parameterValueGenerator.emitPushPtrArrayAndOffset(mv);
  }
}
