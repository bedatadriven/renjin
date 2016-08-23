package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class RecordArrayField extends FieldStrategy {
  
  private Type declaringClass;
  private String name;
  private RecordArrayValueFunction valueFunction;
  private Type arrayType;
  private int arrayLength;

  public RecordArrayField(Type declaringClass, String name,
                          Type elementType, int arrayLength) {
    this.declaringClass = declaringClass;
    this.name = name;
    this.valueFunction = new RecordArrayValueFunction(elementType, arrayLength);
    this.arrayType = Type.getType("[" + elementType.getDescriptor());
    this.arrayLength = arrayLength;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, arrayType.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    JExpr sourceArray = Expressions.field(source, arrayType, name);
    JExpr destArray = Expressions.field(dest, arrayType, name);

    mv.arrayCopy(
        sourceArray, Expressions.constantInt(0),
        destArray, Expressions.constantInt(0),
        Expressions.constantInt(arrayLength));
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JLValue arrayField = Expressions.field(Expressions.thisValue(declaringClass), arrayType, name);
    JExpr newArray = Expressions.newArray(Wrappers.componentType(arrayType), arrayLength);

    arrayField.store(mv, newArray);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    ValueFunction valueFunction = expectedType.getValueFunction();
    
    JLValue arrayField = Expressions.field(instance, arrayType, name);
    JExpr offsetIndex = Expressions.constantInt(offset / 8 / valueFunction.getArrayElementBytes());    

    return valueFunction.dereference(arrayField, offsetIndex);
  }

}
