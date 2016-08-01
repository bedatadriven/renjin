package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class FatPtrFieldStrategy extends FieldStrategy {
  
  private ValueFunction valueFunction;
  private String arrayField;
  private String offsetField;
  private Type arrayType;

  public FatPtrFieldStrategy(ValueFunction valueFunction, String name) {
    this.valueFunction = valueFunction;
    this.arrayField = name;
    this.arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    this.offsetField = name + "$offset";
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public FatPtrPair memberExpr(JExpr instance, int fieldOffset, TypeStrategy expectedType) {
    JExpr array = Expressions.field(instance, arrayType, arrayField);
    JExpr offset = Expressions.field(instance, Type.INT_TYPE, offsetField);
    return new FatPtrPair(array, offset);
  }

}
