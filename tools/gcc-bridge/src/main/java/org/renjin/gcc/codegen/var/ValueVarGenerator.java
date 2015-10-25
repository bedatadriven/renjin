package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

public class ValueVarGenerator extends AbstractExprGenerator implements LValueGenerator, ValueGenerator, VarGenerator {
  private GimpleType type;
  private int localVarIndex;

  public ValueVarGenerator(GimpleType type, int localVarIndex) {
    this.localVarIndex = localVarIndex;
    this.type = type;
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
    
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }
}
