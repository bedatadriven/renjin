package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class RecordArrayField extends FieldStrategy {
  
  private Type declaringClass;
  private String name;
  private RecordArrayValueFunction valueFunction;
  private Type arrayType;
  private int arrayLength;

  public RecordArrayField(Type declaringClass, String name, RecordArrayValueFunction valueFunction, Type arrayType, int arrayLength) {
    this.declaringClass = declaringClass;
    this.name = name;
    this.valueFunction = valueFunction;
    this.arrayType = arrayType;
    this.arrayLength = arrayLength;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, arrayType.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JLValue arrayField = Expressions.field(Expressions.thisValue(declaringClass), arrayType, name);
    JExpr newArray = Expressions.newArray(Wrappers.componentType(arrayType), arrayLength);

    arrayField.store(mv, newArray);
  }

  @Override
  public RecordArrayExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    JLValue arrayField = Expressions.field(instance, arrayType, name);

    return new RecordArrayExpr(valueFunction, arrayField, arrayLength);
  }

}
