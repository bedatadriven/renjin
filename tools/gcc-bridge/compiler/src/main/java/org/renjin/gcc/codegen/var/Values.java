package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.op.PrimitiveBinOpGenerator;
import org.renjin.gcc.gimple.GimpleOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Functions to create value instances
 */
public class Values {

  public static Value newArray(final Type componentType, final int length) {
    return newArray(componentType, constantInt(length));
  }

  public static Value newArray(final WrapperType componentType, final int length) {
    return newArray(componentType.getWrapperType(), length);
  }

  public static Value newArray(Class<?> componentClass, int length) {
    return newArray(Type.getType(componentClass), length);
  }

  public static Value newArray(final Type componentType, final Value length) {
    return new Value() {
      @Override
      public Type getType() {
        return Type.getType("[" + componentType.getDescriptor());
      }

      @Override
      public void load(MethodGenerator mv) {
        length.load(mv);
        mv.newarray(componentType);
      }
    };
  }


  public static Value newArray(Value value, final Value... moreValues) {

    List<Value> values = new ArrayList<>();
    values.add(value);
    values.addAll(Arrays.asList(moreValues));
      
    return newArray(value.getType(), values);
  }

  public static Value newArray(final Type componentType, final List<Value> values) {
    final Type arrayType = Type.getType("[" + componentType.getDescriptor());
    
    // check the types now
    for (int i = 0; i < values.size(); i++) {
      Type elementType = values.get(i).getType();
      if(!elementType.equals(componentType)) {
        throw new IllegalArgumentException(String.format("Invalid type at element %d: %s, expected %s", 
            i, elementType, componentType));
      }
    }

    return new Value() {
      @Override
      public Type getType() {
        return arrayType;
      }

      @Override
      public void load(MethodGenerator mv) {
        mv.iconst(values.size());
        mv.newarray(componentType);
        for (int i = 0; i < values.size(); i++) {
          mv.dup();
          mv.iconst(i);
          values.get(i).load(mv);
          mv.astore(componentType);
        }
      }
    };
  }
  

  public static Value elementAt(final Value array, final Value offset) {
    checkType("array", array, Type.ARRAY);
    checkType("offset", offset, Type.INT_TYPE);
    
    return new Var() {

      @Override
      public Type getType() {
        return array.getType().getElementType();
      }

      @Override
      public void load(MethodGenerator mv) {
        array.load(mv);
        offset.load(mv);
        mv.aload(getType());
      }

      @Override
      public void store(MethodGenerator mv, Value value) {
        array.load(mv);
        offset.load(mv);
        value.load(mv);
        mv.astore(getType());
      }
    };
  }
  
  public static Value elementAt(Value array, final int offset) {
    return elementAt(array, constantInt(offset));
  }


  public static Value nullRef() {
    return new Value() {
      @Override
      public Type getType() {
        return null;
      }

      @Override
      public void load(MethodGenerator mv) {
        mv.aconst(null);
      }
    };
  }

  public static Value constantInt(final int value) {
    return new ConstantValue(Type.INT_TYPE, value);
  }

  public static Value zero() {
    return constantInt(0);
  }

  public static Value zero(final Type type) {
    return new ConstantValue(type, 0);
  }

  public static Value add(final Value x, final Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.PLUS_EXPR, x, y);
  }

  public static Value difference(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MINUS_EXPR, x, y);
  }

  public static Value product(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MULT_EXPR, x, y);
  }

  public static Value divide(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.EXACT_DIV_EXPR, x, y);
  }

  public static Value divide(Value size, int divisor) {
    Preconditions.checkArgument(size.getType().equals(Type.INT_TYPE));

    return divide(size, constantInt(divisor));
  }


  public static Value field(final Value instance, final Type fieldType, final String fieldName) {
    checkType("instance", instance, Type.OBJECT);
    
    return new Value() {
      @Override
      public Type getType() {
        return fieldType;
      }

      @Override
      public void load(MethodGenerator mv) {
        instance.load(mv);
        mv.getfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
      }
    };
  }
  
  private static void checkType(String argName, Value value, int expectedSort) {
    if(value.getType().getSort() != expectedSort) {
      throw new IllegalArgumentException(String.format("Illegal type for %s: %s", argName, value.getType()));
    }
  }
  private static void checkType(String argName, Value value, Type expectedType) {
    if(!value.getType().equals(expectedType)) {
      throw new IllegalArgumentException(String.format("Illegal type %s for %s: Expected %s",
          value.getType(), argName, expectedType));
    }
  }

}