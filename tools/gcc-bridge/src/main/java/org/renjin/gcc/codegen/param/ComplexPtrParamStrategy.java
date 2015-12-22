package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.ComplexPtrVarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy for parameters of type {@code complex *} that uses two arguments: a {@code double} or {@code float}
 * array and an integer offset parameter.
 */
public class ComplexPtrParamStrategy extends ParamStrategy {
  
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
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, int startIndex, LocalVarAllocator localVars) {
    return new ComplexPtrVarGenerator(type, startIndex, startIndex+1);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    // Need to push both the double[] array
    // and the int offset
    parameterValueGenerator.emitPushPtrArrayAndOffset(mv);
  }
}
