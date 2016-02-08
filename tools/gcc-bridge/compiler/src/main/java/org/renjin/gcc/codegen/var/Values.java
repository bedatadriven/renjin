package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.op.PrimitiveBinOpGenerator;
import org.renjin.gcc.gimple.GimpleOp;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
      @Nonnull
      @Override
      public Type getType() {
        return Type.getType("[" + componentType.getDescriptor());
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
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
    return newArray(componentType, values.size(), values);
  }

  public static Value newArray(final Type componentType, final int arrayLength, final List<Value> values) {
    Preconditions.checkNotNull(componentType, "componentType");
    
    if(values.size() > arrayLength) {
      throw new IllegalArgumentException(
          String.format("Number of initial values supplied (%d) is greater than array length (%d)",
              values.size(),
              arrayLength));
    }
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
      @Nonnull
      @Override
      public Type getType() {
        return arrayType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.iconst(arrayLength);
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

      @Nonnull
      @Override
      public Type getType() {
        return array.getType().getElementType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
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


  public static Value nullRef(final Type type) {
    return new Value() {
      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
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

  public static Value sum(final Value x, final Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.PLUS_EXPR, x, y);
  }

  public static Value difference(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MINUS_EXPR, x, y);
  }

  public static Value difference(Value x, int y) {
    if(y == 0) {
      return x;
    } else {
      return difference(x, constantInt(y));
    }
  }

  public static Value product(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MULT_EXPR, x, y);
  }
  
  public static Value product(Value x, int y) {
    Preconditions.checkArgument(x.getType().equals(Type.INT_TYPE));
    
    if(y == 0) {
      return zero(x.getType());
    } else if(y == 1) {
      return x;
    } else {
      return product(x, constantInt(y));
    }
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
    
    return new Var() {


      @Nonnull
      @Override
      public Type getType() {
        return fieldType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        instance.load(mv);
        mv.getfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
      }

      @Override
      public void store(MethodGenerator mv, Value value) {
        instance.load(mv);
        value.load(mv);
        mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
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

  public static Value cast(final Value object, final Type type) {
    
    // Can we reduce this to a NOOP ?
    if(object.getType().equals(type)) {
      return object;
    }
    // Verify that this is in the realm of possibility
    checkCast(object.getType(), type);
    
    return new Var() {

      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        object.load(mv);
        mv.checkcast(type);
      }
      
      @Override
      @SuppressWarnings("unchecked")
      public void store(MethodGenerator mv, Value value) {
        if(!(object instanceof LValue)) {
          throw new UnsupportedOperationException();
        }
        ((LValue) object).store(mv, value);
      }
    };
  }

  private static void checkCast(Type fromType, Type toType) {
    if(toType.getSort() != Type.OBJECT && 
       toType.getSort() != Type.ARRAY) {
      throw new IllegalArgumentException("Target type for cast must be an array or object: " + toType);
    }
    int fromSort = fromType.getSort();
    int toSort = toType.getSort();
    if(fromSort != toSort) {
      throw new IllegalArgumentException("Invalid cast from " + fromType + " to " + toType);
    }
    if(fromSort == Type.ARRAY) {
      checkCast(fromType.getElementType(), toType.getElementType());
    }
  }

  public static Value voidValue() {
    return new Value() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.VOID_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        // LOAD NOTHING
      }
    };
  }

  public static Value thisValue(final Type type) {
    return new Value() {

      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.load(0, type);
      }
    };
  }


}