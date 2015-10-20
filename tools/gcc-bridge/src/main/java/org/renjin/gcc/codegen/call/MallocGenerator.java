package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IDIV;

/**
 * Generates a {@code malloc} call
 */
public class MallocGenerator extends AbstractExprGenerator implements PtrGenerator {
  private final GimpleType gimpleBaseType;
  private final ValueGenerator sizeGenerator;
  private final WrapperType pointerType;

  public MallocGenerator(LValueGenerator lhs, ExprGenerator sizeGenerator) {
    this.pointerType = lhs.getPointerType();
    this.gimpleBaseType = lhs.getGimpleType().getBaseType();
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
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    // first calculate the size of the array from the argument,
    // which is in bytes
    sizeGenerator.emitPushValue(mv);
    mv.visitLdcInsn(gimpleBaseType.sizeOf());
    mv.visitInsn(IDIV);

    Type baseType = pointerType.getBaseType();
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

  @Override
  public GimpleType getGimpleType() {
    throw new UnsupportedOperationException();
  }
}