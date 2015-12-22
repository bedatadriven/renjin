package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.ComplexArrayPtrVarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimplePointerType;

import java.util.Arrays;
import java.util.List;


/**
 * Strategy for parameters which are pointers to array of complex values that uses two JVM arguments:
 * one for an array of {@code double} or {@code float}s, and one for an offset into the array.
 */
public class ComplexArrayPtrParamStrategy extends ParamStrategy {

  private GimplePointerType type;
  private GimpleArrayType arrayType;
  private GimpleComplexType componentType;

  public ComplexArrayPtrParamStrategy(GimplePointerType type) {
    this.type = type;
    this.arrayType = type.getBaseType();
    this.componentType = (GimpleComplexType) arrayType.getComponentType();
  }

  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(componentType.getJvmPartArrayType(), Type.INT_TYPE);
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, int startIndex, 
                                          LocalVarAllocator localVars) {
    return new ComplexArrayPtrVarGenerator(type, startIndex, startIndex+1);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    throw new UnsupportedOperationException();
  }
}
