package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleStringConstant;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.BytePtr;

/**
 * Emits the byte code to push string literals onto the stack
 * 
 */
public class StringLiteralGenerator implements PtrGenerator {

  private final GimpleStringConstant constantExpr;

  public StringLiteralGenerator(GimpleExpr value) {
    this.constantExpr = (GimpleStringConstant) value;
  }

  @Override
  public GimpleType gimpleBaseType() {
    return constantExpr.getType();
  }

  @Override
  public Type baseType() {
    return Type.BYTE_TYPE;
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    
    mv.visitLdcInsn(constantExpr.getValue());
    
    // consume the string constant and push the array reference
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getType(BytePtr.class).getInternalName(),
          "toArray", "(Ljava/lang/String;)[B", false);
    
    // push the offset
    mv.visitInsn(Opcodes.ICONST_0);
  }
}
