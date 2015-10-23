package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;

public abstract class AbstractExprGenerator implements ExprGenerator {

  @Override
  public ExprGenerator valueOf() {
    throw new UnsupportedOperationException(String.format("%s [%s] cannot be dereferenced",
        toString(), getClass().getSimpleName()));
  }

  @Override
  public ExprGenerator addressOf() {
    throw new UnsupportedOperationException(String.format("%s [%s] is not addressable",
        toString(), getClass().getSimpleName()));
  }

  @Override
  public ExprGenerator realPart() {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a complex number value",
        toString(), getClass().getSimpleName()));
  }

  @Override
  public ExprGenerator imaginaryPart() {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a complex number value",
        toString(), getClass().getSimpleName()));  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    throw new UnsupportedOperationException(String.format("%s [%s] is not an array", 
        toString(), getClass().getSimpleName()));
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a value type",
        toString(), getClass().getSimpleName()));
  
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a [array-backed] pointer type",
        toString(), getClass().getSimpleName()));  
  }

  @Override
  public void emitPushMethodHandle(MethodVisitor mv) {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a [reference-backed] pointer type",
        toString(), getClass().getSimpleName()));
  }

  @Override
  public WrapperType getPointerType() {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a pointer type",
        toString(), getClass().getSimpleName())); 
  }

  @Override
  public void emitPushPointerWrapper(MethodVisitor mv) {
    getPointerType().emitPushNewWrapper(mv, this);
  }

  @Override
  public void emitPushComplexAsArray(MethodVisitor mv) {
    Type partType = realPart().getValueType();
    
    mv.visitInsn(Opcodes.ICONST_2);
    MallocGenerator.emitNewArray(mv, partType);
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_0);
    realPart().emitPushValue(mv);
    mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_1);
    imaginaryPart().emitPushValue(mv);
    mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException(String.format("Cannot store value to %s [%s]",
        toString(), getClass().getSimpleName())); 
  }

  @Override
  public Type getValueType() {
    throw new UnsupportedOperationException(String.format("%s [%s] is not a value type",
        toString(), getClass().getSimpleName()));
  }
}
