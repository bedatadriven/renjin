package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.ComplexArrayPtrVarGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;

import java.util.Arrays;
import java.util.List;


public class ComplexArrayPtrParamGenerator extends ParamGenerator {

  private GimplePointerType type;

  public ComplexArrayPtrParamGenerator(GimplePointerType type) {
    this.type = type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(Type.getType(double[].class), Type.INT_TYPE);
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, int startIndex, LocalVarAllocator localVars) {
    return new ComplexArrayPtrVarGenerator(type, startIndex, startIndex+1);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    throw new UnsupportedOperationException();
  }
}
