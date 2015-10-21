package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Writes jimple instructions to store and retrieve a single primitive numeric
 * value on the JVM heap, by allocating a unit array. Variables stored this way
 * can be addressed and passed by reference to other methods.
 *
 */
public class AddressableVarGenerator extends AbstractExprGenerator implements VarGenerator {
  private int index;
  private String name;
  private GimpleType type;
  private Type componentType;
  
  public AddressableVarGenerator(int index, String name, GimpleType type) {
    this.index = index;
    this.name = name;
    this.type = type;
    componentType = ((GimplePrimitiveType) type).jvmType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ICONST_1);
    MallocGenerator.emitNewArray(mv, componentType);
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  @Override
  public Type getValueType() {
    return componentType;
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, index);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitInsn(componentType.getOpcode(Opcodes.IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    mv.visitVarInsn(Opcodes.ALOAD, index);
    mv.visitInsn(Opcodes.ICONST_0);
    valueGenerator.emitPushValue(mv);
    mv.visitInsn(componentType.getOpcode(Opcodes.IASTORE));
  }

  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(type);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.of(componentType);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, index);
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public ExprGenerator valueOf() {
      return AddressableVarGenerator.this;
    }
  }
}
