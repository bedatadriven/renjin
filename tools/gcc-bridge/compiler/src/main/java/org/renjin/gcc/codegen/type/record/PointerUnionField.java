package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.*;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

/**
 * A field that is the union of more than one pointer types.
 * We store the value as a single Object instance.
 */
public class PointerUnionField extends SingleFieldStrategy {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  
  public PointerUnionField(Type declaringClass, String fieldName) {
    super(declaringClass, fieldName, OBJECT_TYPE);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new UnsupportedOperationException("TODO: offset = " + offset);
    }
    
    JLValue fieldExpr = Expressions.field(instance, Type.getType(Object.class), fieldName);

    if(expectedType == null) {
      return new VoidPtr(fieldExpr);
    }

    if(expectedType instanceof FatPtrStrategy) {
      return new FatPtrMemberExpr(fieldExpr, expectedType.getValueFunction());
    } 
    throw new UnsupportedOperationException(String.format("TODO: strategy = %s", expectedType));
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr count) {
    memsetReference(mv, instance, byteValue, count);
  }

  private class FatPtrMemberExpr implements FatPtr {

    private JLValue fieldExpr;
    private ValueFunction valueFunction;

    public FatPtrMemberExpr(JLValue fieldExpr, ValueFunction valueFunction) {
      this.fieldExpr = fieldExpr;
      this.valueFunction = valueFunction;
    }

    @Override
    public Type getValueType() {
      return valueFunction.getValueType();
    }

    @Override
    public boolean isAddressable() {
      return false;
    }

    @Override
    public JExpr wrap() {
      return fieldExpr;
    }

    @Override
    public FatPtrPair toPair(MethodGenerator mv) {
      Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
      JExpr wrapper = Expressions.cast(fieldExpr, wrapperType);

      return Wrappers.toPair(mv, valueFunction, wrapper);
    }

    @Override
    public void store(MethodGenerator mv, GExpr rhs) {
      if(rhs instanceof FatPtr) {
        fieldExpr.store(mv, ((FatPtr) rhs).wrap());
      } else {
        throw new UnsupportedOperationException("TODO: " + rhs.getClass().getName());
      }
    }

    @Override
    public GExpr addressOf() {
      throw new NotAddressableException();
    }

    @Override
    public void jumpIfNull(MethodGenerator mv, Label label) {
      throw new UnsupportedOperationException("TODO");
    }

    @Override
    public GExpr valueOf() {
      throw new UnsupportedOperationException("TODO");
    }
  }
  
}
