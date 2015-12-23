package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Writes jimple instructions to store and retrieve a single primitive numeric
 * value on the JVM heap, by allocating a unit array. Variables stored this way
 * can be addressed and passed by reference to other methods.
 *
 */
public class AddressablePrimitiveVarGenerator extends AbstractExprGenerator implements VarGenerator {
  private Var arrayVar;
  private GimpleType type;
  private Type componentType;
  
  public AddressablePrimitiveVarGenerator(GimpleType type, Var arrayVar) {
    this.arrayVar = arrayVar;
    this.type = type;
    componentType = ((GimplePrimitiveType) type).jvmType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    mv.visitInsn(Opcodes.ICONST_1);
    MallocGenerator.emitNewArray(mv, componentType);
    arrayVar.store(mv);
    
    if(initialValue.isPresent() && !defaultValue(initialValue.get())) {
      emitStore(mv, initialValue.get());
    }
  }

  private boolean defaultValue(ExprGenerator exprGenerator) {
    // TODO: check for zero: array is already initialized to zero
    return false;
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }
  

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    arrayVar.load(mv);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitInsn(componentType.getOpcode(Opcodes.IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    arrayVar.load(mv);
    mv.visitInsn(Opcodes.ICONST_0);
    valueGenerator.emitPrimitiveValue(mv);
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
      arrayVar.load(mv);
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public ExprGenerator valueOf() {
      return AddressablePrimitiveVarGenerator.this;
    }
  }
}
