package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;


public class RecordArrayField extends FieldStrategy {
  
  private Type declaringClass;
  private String name;
  private Type arrayType;
  private int arrayLength;

  public RecordArrayField(Type declaringClass, String name, Type arrayType, int arrayLength) {
    this.declaringClass = declaringClass;
    this.name = name;
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
  public RecordArrayExpr memberExprGenerator(JExpr instance) {
    JLValue arrayField = Expressions.field(instance, arrayType, name);

    return new RecordArrayExpr(arrayField, arrayLength);
  }

}
