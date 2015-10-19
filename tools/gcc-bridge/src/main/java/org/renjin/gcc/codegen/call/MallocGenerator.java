package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IDIV;

/**
 * Generates a {@code malloc} call
 */
public class MallocGenerator implements PtrGenerator {
  private final GimpleType gimpleBaseType;
  private final Type baseType;
  private final ValueGenerator sizeGenerator;

  public MallocGenerator(LValueGenerator lhs, ExprGenerator sizeGenerator) {
    PtrGenerator ptr = (PtrGenerator) lhs;
    this.gimpleBaseType = ptr.gimpleBaseType();
    this.baseType = ptr.baseType();
    this.sizeGenerator = (ValueGenerator) sizeGenerator;
  }

  public static boolean isMalloc(GimpleExpr functionExpr) {
    if (functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return ref.getName().equals("malloc");
      }
    }
    return false;
  }

  @Override
  public GimpleType gimpleBaseType() {
    return gimpleBaseType;
  }

  @Override
  public Type baseType() {
    return baseType;
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    // first calculate the size of the array from the argument,
    // which is in bytes
    sizeGenerator.emitPush(mv);
    mv.visitLdcInsn(gimpleBaseType.sizeOf());
    mv.visitInsn(IDIV);

    // now create the array
    if(baseType.equals(Type.INT_TYPE)) {
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
    } else if(baseType.equals(Type.LONG_TYPE)) {
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
    } else if(baseType.equals(Type.FLOAT_TYPE)) {
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
    } else if(baseType.equals(Type.DOUBLE_TYPE)) {
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
    } else {
      throw new UnsupportedOperationException("type: " + baseType);
    }

    mv.visitInsn(ICONST_0);
  }
}