package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IDIV;

/**
 * Generates a {@code malloc} call
 */
public class MallocGenerator extends AbstractExprGenerator implements ExprGenerator {
  private Type elementType;
  private int elementSize;
  private final ExprGenerator totalSizeGenerator;

  /**
   * 
   * @param baseType the JVM type to be allocated
   * @param baseTypeSize the size, in bytes of the original Gimple base type
   * @param sizeGenerator an expression generator for the total number of bytes to allocate
   */
  public MallocGenerator(Type baseType, int baseTypeSize, ExprGenerator sizeGenerator) {
    this.elementType = baseType;
    this.elementSize = baseTypeSize;
    this.totalSizeGenerator = sizeGenerator;
  }

  public static boolean isMalloc(GimpleExpr functionExpr) {
    return isFunctionNamed(functionExpr, "malloc")  ||
           isFunctionNamed(functionExpr, "__builtin_malloc");
  }


  public static boolean isFree(GimpleExpr functionExpr) {
    return isFunctionNamed(functionExpr, "__builtin_free");
  }
  
  public static boolean isRealloc(GimpleExpr functionExpr) {
    return isFunctionNamed(functionExpr, "realloc");
  }

  private static boolean isFunctionNamed(GimpleExpr functionExpr, String name) {
    if (functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return ref.getName().equals(name);
      }
    }
    return false;
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    // first calculate the size of the array from the argument,
    // which is in bytes
    totalSizeGenerator.emitPrimitiveValue(mv);
    PrimitiveConstValueGenerator.emitInt(mv, elementSize);
    mv.visitInsn(IDIV);

    // now create the array
    emitNewArray(mv, elementType);

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
      case Type.OBJECT:
        mv.visitTypeInsn(Opcodes.ANEWARRAY, componentType.getInternalName());
        break;
      
      default:
        throw new UnsupportedOperationException("type: " + componentType);
    }
  }

  @Override
  public WrapperType getPointerType() {
    return super.getPointerType();
  }

  @Override
  public GimpleType getGimpleType() {
    throw new UnsupportedOperationException();
  }

}