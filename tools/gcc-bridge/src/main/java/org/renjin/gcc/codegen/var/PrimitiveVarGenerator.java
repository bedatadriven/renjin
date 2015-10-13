package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

public class PrimitiveVarGenerator implements LValueGenerator, PrimitiveGenerator, VarGenerator {
  private int index;
  private String name;
  private GimplePrimitiveType primitiveType;

  public PrimitiveVarGenerator(int index, String name, GimplePrimitiveType primitiveType) {
    this.index = index;
    this.name = name;
    this.primitiveType = primitiveType;
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    PrimitiveGenerator primitiveGenerator = (PrimitiveGenerator) valueGenerator;
    
    Preconditions.checkArgument(primitiveGenerator.primitiveType().equals(primitiveType()),
        "Type mismatch: Cannot assign %s of type %s to %s of type %s",
        primitiveGenerator,
        primitiveGenerator.primitiveType(),
        this,
        primitiveType());
    
    primitiveGenerator.emitPush(mv);
    
    mv.visitVarInsn(primitiveType().getOpcode(ISTORE), index);
  }

  @Override
  public Type primitiveType() {
    return primitiveType.jvmType();
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    mv.visitVarInsn(primitiveType().getOpcode(ILOAD), index);
  }

  @Override
  public String toString() {
    return name;
  }
}
