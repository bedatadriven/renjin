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
        return ref.getName().equals("malloc") ||
               ref.getName().equals("__builtin_malloc");
      }
    }
    return false;
  }


  public static boolean isFree(GimpleExpr functionExpr) {
    if (functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return ref.getName().equals("__builtin_free");
      }
    }
    return false;

  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    // first calculate the size of the array from the argument,
    // which is in bytes
    sizeGenerator.emitPrimitiveValue(mv);
    mv.visitLdcInsn(gimpleBaseType.sizeOf());
    mv.visitInsn(IDIV);

    // now create the array
    emitNewArray(mv, pointerType.getBaseType());

    mv.visitInsn(ICONST_0);
  }

  public static void emitNewArray(MethodVisitor mv, Type componentType) {
    switch (componentType.getSort()) {
      case Type.BOOLEAN:
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
        break;
      case Type.INT:
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        break;
      case Type.LONG:
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
        break;
      case Type.FLOAT:
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
        break;
      case Type.DOUBLE:
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
        break;
      default:
        throw new UnsupportedOperationException("type: " + componentType);
    }
  }

  @Override
  public GimpleType getGimpleType() {
    throw new UnsupportedOperationException();
  }

}