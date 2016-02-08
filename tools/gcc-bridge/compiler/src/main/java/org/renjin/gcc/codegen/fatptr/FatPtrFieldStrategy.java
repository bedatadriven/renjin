package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;


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
  public Expr memberExprGenerator(SimpleExpr instance) {
    SimpleExpr array = Expressions.field(instance, arrayType, arrayField);
    SimpleExpr offset = Expressions.field(instance, Type.INT_TYPE, offsetField);
    return new FatPtrExpr(array, offset);
  }
}
