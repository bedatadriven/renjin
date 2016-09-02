package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class FatPtrFieldStrategy extends FieldStrategy {

  private Type ownerClass;
  private ValueFunction valueFunction;
  private String arrayField;
  private String offsetField;
  private Type arrayType;

  public FatPtrFieldStrategy(Type ownerClass, ValueFunction valueFunction, String name, Type arrayType) {
    this.ownerClass = ownerClass;
    this.valueFunction = valueFunction;
    this.arrayField = name;
    this.offsetField = name + "$offset";
    this.arrayType = arrayType;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public FatPtrPair memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    return memberExpr(instance);
  }

  private FatPtrPair memberExpr(JExpr instance) {
    JExpr arrayExpr = Expressions.field(instance, arrayType, arrayField);
    JExpr offsetExpr = Expressions.field(instance, Type.INT_TYPE, offsetField);
    return new FatPtrPair(valueFunction, arrayExpr, offsetExpr);
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    FatPtrPair sourceExpr = memberExpr(source);
    FatPtrPair destExpr = memberExpr(dest);
    destExpr.store(mv, sourceExpr);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    // Any value will lead to a garbage pointer, so we assume
    // that assigning NULL will have the same effect
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(ownerClass, arrayField, arrayType);
    
    instance.load(mv);
    byteValue.load(mv);
    mv.putfield(ownerClass, offsetField, Type.INT_TYPE);
  }

}
