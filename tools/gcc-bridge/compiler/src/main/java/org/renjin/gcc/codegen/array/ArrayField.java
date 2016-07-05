package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrMalloc;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for array fields
 */
public class ArrayField extends FieldStrategy {

  private Type declaringClass;
  private String name;
  private int arrayLength;
  private final ValueFunction valueFunction;
  private final Type arrayType;

  public ArrayField(Type declaringClass, String name, int arrayLength, ValueFunction valueFunction) {
    this.declaringClass = declaringClass;
    this.name = name;
    this.arrayLength = arrayLength;
    this.valueFunction = valueFunction;
    this.arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, arrayType.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JExpr newArray = FatPtrMalloc.allocArray(mv, valueFunction, Expressions.constantInt(arrayLength));
    JLValue arrayField = Expressions.field(Expressions.thisValue(declaringClass), arrayType, name);
    
    arrayField.store(mv, newArray);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    JExpr array = Expressions.field(instance, arrayType, name);
    JExpr offset = Expressions.zero();
    return new ArrayExpr(valueFunction, arrayLength, array, offset);
  }
}
