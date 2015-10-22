package org.renjin.gcc.codegen.param;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.ComplexPtrVarGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Arrays;
import java.util.List;

/**
 * Generates a parameter for a pointer to a complex number value.
 * 
 * <p>This is compiled as two parameters: a double[] array and an integer offset</p>
 */
public class ComplexPtrParamGenerator extends ParamGenerator {
  
  private GimpleType type;

  public ComplexPtrParamGenerator(GimpleType type) {
    // as far as I know we're only dealing with 64-bit complex numbers
    // make sure this assumptions is correct
    Preconditions.checkArgument(type.getBaseType() instanceof GimpleComplexType);
    Preconditions.checkArgument(type.getBaseType().sizeOf() == 128, "Expected only double precision complex numbers");
    
    this.type = type;
  }

  @Override
  public int numSlots() {
    return 2;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(Type.getType(double[].class), Type.getType(int.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, int startIndex, LocalVarAllocator localVars) {
    return new ComplexPtrVarGenerator(type, startIndex, startIndex+1);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    // Need to push both the double[] array
    // and the int offset
    parameterValueGenerator.emitPushPtrArrayAndOffset(mv);
  }
}
