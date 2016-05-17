package org.renjin.gcc.codegen.expr;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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

import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * Static utility methods pertaining to create and compose {@link Expr}s
 */
public class Expressions {

  public static SimpleExpr newArray(final Type componentType, final int length) {
    return newArray(componentType, constantInt(length));
  }

  public static SimpleExpr newArray(final WrapperType componentType, final int length) {
    return newArray(componentType.getWrapperType(), length);
  }

  public static SimpleExpr newArray(Class<?> componentClass, int length) {
    return newArray(Type.getType(componentClass), length);
  }
  
  public static SimpleExpr newArray(final Type componentType, final SimpleExpr length) {
    return new SimpleExpr() {
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
  
  public static SimpleExpr newArray(SimpleExpr value, final SimpleExpr... moreValues) {

    List<SimpleExpr> values = new ArrayList<>();
    values.add(value);
    values.addAll(Arrays.asList(moreValues));
      
    return newArray(value.getType(), values);
  }

  public static SimpleExpr newArray(final Type componentType, final List<SimpleExpr> values) {
    return newArray(componentType, values.size(), values);
  }


  public static SimpleExpr newArray(Type valueType, int elementLength, Optional<SimpleExpr> firstValue) {
    List<SimpleExpr> initialValues = Lists.newArrayList();
    if(firstValue.isPresent()) {
      initialValues.add(firstValue.get());
    }
    return newArray(valueType, elementLength, initialValues);
  }

  public static SimpleExpr newArray(final Type componentType, final int arrayLength, final List<SimpleExpr> values) {
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
      if(elementType.getSort() != componentType.getSort()) {
        throw new IllegalArgumentException(String.format("Invalid type at element %d: %s, expected %s",
            i, elementType, componentType));
      }
    }

    return new SimpleExpr() {
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


  public static SimpleExpr elementAt(SimpleExpr array, final int offset) {
    return elementAt(array, constantInt(offset));
  }

  public static SimpleExpr elementAt(final SimpleExpr array, final SimpleExpr offset) {
    checkType("array", array, Type.ARRAY);
    checkType("offset", offset, Type.INT_TYPE);
    
    return new ArrayElement(array, offset);
  }

  public static SimpleExpr nullRef(final Type type) {
    return new SimpleExpr() {
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

  public static SimpleExpr constantInt(final int value) {
    return new ConstantValue(Type.INT_TYPE, value);
  }

  public static SimpleExpr zero() {
    return constantInt(0);
  }

  public static SimpleExpr zero(final Type type) {
    return new ConstantValue(type, 0);
  }

  public static SimpleExpr sum(final SimpleExpr x, final SimpleExpr y) {
    return new PrimitiveBinOpGenerator(GimpleOp.PLUS_EXPR, x, y);
  }
  
  public static SimpleExpr sum(SimpleExpr x, int y) {
    if(y == 0) {
      return x;
    } else {
      return sum(x, constantInt(y));
    }
  }

  public static SimpleExpr difference(SimpleExpr x, SimpleExpr y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MINUS_EXPR, x, y);
  }

  public static SimpleExpr difference(SimpleExpr x, int y) {
    if(y == 0) {
      return x;
    } else {
      return difference(x, constantInt(y));
    }
  }

  public static SimpleExpr product(SimpleExpr x, SimpleExpr y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MULT_EXPR, x, y);
  }
  
  public static SimpleExpr product(SimpleExpr x, int y) {
    Preconditions.checkArgument(x.getType().equals(Type.INT_TYPE));
    
    if(y == 0) {
      return zero(x.getType());
    } else if(y == 1) {
      return x;
    } else {
      return product(x, constantInt(y));
    }
  }

  public static SimpleExpr divide(SimpleExpr x, SimpleExpr y) {
    return new PrimitiveBinOpGenerator(GimpleOp.EXACT_DIV_EXPR, x, y);
  }

  public static SimpleExpr divide(SimpleExpr size, int divisor) {
    Preconditions.checkArgument(size.getType().equals(Type.INT_TYPE));

    return divide(size, constantInt(divisor));
  }

  public static SimpleLValue field(final SimpleExpr instance, final Type fieldType, final String fieldName) {
    checkType("instance", instance, Type.OBJECT);
    
    return new SimpleLValue() {
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
      public void store(MethodGenerator mv, SimpleExpr value) {
        instance.load(mv);
        value.load(mv);
        mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
      }
    };
  }
  
  private static void checkType(String argName, SimpleExpr value, int expectedSort) {
    if(value.getType().getSort() != expectedSort) {
      throw new IllegalArgumentException(String.format("Illegal type for %s: %s", argName, value.getType()));
    }
  }
  private static void checkType(String argName, SimpleExpr value, Type expectedType) {
    if(!value.getType().equals(expectedType)) {
      throw new IllegalArgumentException(String.format("Illegal type %s for %s: Expected %s",
          value.getType(), argName, expectedType));
    }
  }

  public static SimpleExpr cast(final SimpleExpr object, final Type type) {
    
    // Can we reduce this to a NOOP ?
    if(object.getType().equals(type)) {
      return object;
    }
    // Verify that this is in the realm of possibility
    checkCast(object.getType(), type);
    
    return new SimpleLValue() {

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
      public void store(MethodGenerator mv, SimpleExpr value) {
        if(!(object instanceof LValue)) {
          throw new UnsupportedOperationException();
        }
        ((LValue) object).store(mv, value);
      }
    };
  }

  private static void checkCast(Type fromType, Type toType) {
    if (toType.getSort() != Type.OBJECT && 
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

  public static SimpleExpr voidValue() {
    return new SimpleExpr() {
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

  public static SimpleExpr thisValue(final Type type) {
    return new SimpleExpr() {

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

  public static boolean isPrimitive(SimpleExpr simpleExpr) {
    return isPrimitive(simpleExpr.getType());
  }

  public static boolean isPrimitive(Type type) {
    switch (type.getSort()) {
      case Type.BOOLEAN:
      case Type.BYTE:
      case Type.SHORT:
      case Type.CHAR:
      case Type.INT:
      case Type.LONG:
      case Type.FLOAT:
      case Type.DOUBLE:
        return true;
      
      case Type.OBJECT:
      case Type.ARRAY:
      case Type.METHOD:
        return false;
     
      default:
        throw new IllegalArgumentException("type: " + type);
    }
  }

  public static SimpleExpr box(final SimpleExpr simpleExpr) {
    Preconditions.checkArgument(isPrimitive(simpleExpr), "simpleExpr must be a primitive");
    
    Type primitiveType = simpleExpr.getType();
    final Type boxedType = boxedType(primitiveType);
    final String valueOfDescriptor = getMethodDescriptor(boxedType, primitiveType);
    
    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return boxedType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        simpleExpr.load(mv);
        mv.invokestatic(boxedType.getInternalName(), "valueOf", valueOfDescriptor, false);    
      }
    };
  }
  
  public static Type boxedType(Type type) {
    switch (type.getSort()) {
      case Type.BOOLEAN:
        return Type.getType(Boolean.class);
      case Type.BYTE:
        return Type.getType(Byte.class);
      case Type.SHORT:
        return Type.getType(Short.class);
      case Type.CHAR:
        return Type.getType(Character.class);
      case Type.INT:
        return Type.getType(Integer.class);
      case Type.LONG:
        return Type.getType(Long.class);
      case Type.FLOAT:
        return Type.getType(Float.class);
      case Type.DOUBLE:
        return Type.getType(Double.class);
      
      default:
        throw new IllegalArgumentException("type: " + type);
    }
  }

  public static SimpleExpr copyOfArrayRange(final SimpleExpr array, final SimpleExpr from, final SimpleExpr to) {

    final Type arrayType = array.getType();

    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return arrayType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        // static byte[] copyOfRange(byte[] original, int from, int to);
        
        array.load(mv);
        from.load(mv);
        to.load(mv);
        mv.invokestatic(Arrays.class, "copyOfRange", 
            getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE, Type.INT_TYPE));
      }
    };
  }

  public static SimpleExpr newObject(final Type classType) {
    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return classType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.anew(classType);
        mv.dup();
        mv.invokeconstructor(classType);
      }
    };
  }
}