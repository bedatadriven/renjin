package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleStringConstant;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.BytePtr;

/**
 * Emits the byte code to push string literals onto the stack
 * 
 */
public class StringLiteralGenerator extends AbstractExprGenerator implements PtrGenerator {

  private final GimpleStringConstant constantExpr;
  private final GimpleArrayType type;

  public StringLiteralGenerator(GimpleExpr value) {
    this.constantExpr = (GimpleStringConstant) value;
    this.type = constantExpr.getType();
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    
    mv.visitLdcInsn(constantExpr.getValue());
    
    // consume the string constant and push the array reference
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getType(BytePtr.class).getInternalName(),
          "toArray", "(Ljava/lang/String;)[B", false);
    
    // push the offset
    mv.visitInsn(Opcodes.ICONST_0);
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public WrapperType getPointerType() {
    return WrapperType.of(Type.BYTE_TYPE);
  }
}
