package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

public class PrimitiveVarGenerator extends AbstractExprGenerator implements LValueGenerator, ValueGenerator, VarGenerator {
  private GimplePrimitiveType type;
  private int localVarIndex;

  public PrimitiveVarGenerator(GimpleType type, int localVarIndex) {
    this.localVarIndex = localVarIndex;
    this.type = (GimplePrimitiveType) type;
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPrimitiveValue(mv);

    Preconditions.checkArgument(checkTypes(valueGenerator),
        "Type mismatch: Cannot assign %s of type %s to %s of type %s",
        valueGenerator,
        valueGenerator.getJvmPrimitiveType(),
        this,
        getJvmPrimitiveType());
    
    mv.visitVarInsn(getJvmPrimitiveType().getOpcode(ISTORE), localVarIndex);

  }

  private boolean checkTypes(ExprGenerator valueGenerator) {
    Type varType = getJvmPrimitiveType();
    Type valueType = valueGenerator.getJvmPrimitiveType();
 
    return (isIntType(varType) && isIntType(valueType)) ||
           varType.equals(valueType);
  }

  private boolean isIntType(Type type) {
    return type.equals(Type.BOOLEAN_TYPE) ||
           type.equals(Type.BYTE_TYPE) ||
           type.equals(Type.INT_TYPE);
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    mv.visitVarInsn(getJvmPrimitiveType().getOpcode(ILOAD), localVarIndex);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    // this is pretty annoying, but we have to initialize local variables here.
    // GCC will compile without error if an uninitialized variable *could* used, but 
    // the JVM will refuse to verify the code.
    Type primitiveType = getJvmPrimitiveType();
    if(primitiveType.equals(Type.FLOAT_TYPE)) {
      mv.visitInsn(FCONST_0);
    } else if(primitiveType.equals(Type.DOUBLE_TYPE)) {
      mv.visitInsn(DCONST_0);
    } else if(primitiveType.equals(Type.LONG_TYPE)) {
      mv.visitInsn(LCONST_0);
    } else {
      mv.visitInsn(ICONST_0);
    }
    mv.visitVarInsn(primitiveType.getOpcode(ISTORE), localVarIndex);
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }
}
