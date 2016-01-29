package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
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
public class ComplexArrayPtrParamStrategy implements ParamStrategy {

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
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, 
                                          VarAllocator localVars) {
    return new ComplexArrayPtrVarGenerator(type, paramVars.get(0), paramVars.get(1));
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator parameterValueGenerator) {
    throw new UnsupportedOperationException();
  }
}
