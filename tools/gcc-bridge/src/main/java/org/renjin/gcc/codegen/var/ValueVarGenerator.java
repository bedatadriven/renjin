package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.ValueTypes;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

public class ValueVarGenerator implements LValueGenerator, ValueGenerator, VarGenerator {
  private int index;
  private String name;
  private Type type;

  public ValueVarGenerator(int index, String name, GimpleType type) {
    this.index = index;
    this.name = name;
    this.type = ValueTypes.typeOf(type);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator exprGenerator) {
    ValueGenerator valueGenerator = (ValueGenerator) exprGenerator;
    
    Preconditions.checkArgument(checkTypes(valueGenerator),
        "Type mismatch: Cannot assign %s of type %s to %s of type %s",
        valueGenerator,
        valueGenerator.primitiveType(),
        this,
        primitiveType());
    
    valueGenerator.emitPush(mv);
    
    mv.visitVarInsn(primitiveType().getOpcode(ISTORE), index);
  }

  private boolean checkTypes(ValueGenerator valueGenerator) {
    Type varType = primitiveType();
    Type valueType = valueGenerator.primitiveType();
 
    return (isIntType(varType) && isIntType(valueType)) ||
           varType.equals(valueType);
  }

  private boolean isIntType(Type type) {
    return type.equals(Type.BOOLEAN_TYPE) ||
           type.equals(Type.BYTE_TYPE) ||
           type.equals(Type.INT_TYPE);
  }
  
  @Override
  public Type primitiveType() {
    return type;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    mv.visitVarInsn(primitiveType().getOpcode(ILOAD), index);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
  }
}
