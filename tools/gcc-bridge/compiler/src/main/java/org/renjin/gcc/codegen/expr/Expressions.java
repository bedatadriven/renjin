/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.renjin.repackaged.asm.Type.*;

/**
 * Static utility methods pertaining to create and compose {@link GExpr}s
 */
public class Expressions {

  public static JExpr newArray(final Type componentType, final int length) {
    Preconditions.checkArgument(length >= 0);
    return newArray(componentType, constantInt(length));
  }

  public static JExpr newArray(final WrapperType componentType, final int length) {
    return newArray(componentType.getWrapperType(), length);
  }

  public static JExpr newArray(Class<?> componentClass, int length) {
    return newArray(Type.getType(componentClass), length);
  }
  
  public static JExpr newArray(final Type componentType, final JExpr length) {
    checkType("length", length, Type.INT);
    return new JExpr() {
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
  
  public static JExpr newArray(JExpr value, final JExpr... moreValues) {

    List<JExpr> values = new ArrayList<>();
    values.add(value);
    values.addAll(Arrays.asList(moreValues));
      
    return newArray(value.getType(), values);
  }

  public static JExpr newArray(final Type componentType, final List<JExpr> values) {
    return newArray(componentType, values.size(), values);
  }


  public static JExpr newArray(Type valueType, int elementLength, Optional<JExpr> firstValue) {
    List<JExpr> initialValues = Lists.newArrayList();
    if(firstValue.isPresent()) {
      initialValues.add(firstValue.get());
    }
    return newArray(valueType, elementLength, initialValues);
  }

  public static JExpr newArray(final Type componentType, final int arrayLength, final List<JExpr> values) {
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
      if(promoteSmallInts(elementType).getSort() != promoteSmallInts(componentType).getSort()) {
        throw new IllegalArgumentException(String.format("Invalid type at element %d: %s, expected %s",
            i, elementType, componentType));
      }
    }

    return new JExpr() {
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


  public static ArrayElement elementAt(JExpr array, final int offset) {
    return elementAt(array, constantInt(offset));
  }

  public static ArrayElement elementAt(final JExpr array, final JExpr offset) {
    checkType("array", array, Type.ARRAY);
    checkType("offset", offset, Type.INT_TYPE);
    
    return new ArrayElement(array, offset);
  }

  public static JExpr nullRef(final Type type) {
    return new JExpr() {
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

  public static JExpr constantInt(final int value) {
    return new ConstantValue(Type.INT_TYPE, value);
  }

  public static JExpr constantBoolean(final boolean value) {
    return new ConstantValue(Type.BOOLEAN_TYPE, value ? 1 : 0);
  }

  public static JExpr constantLong(final long value) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.LONG_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.visitLdcInsn(value);
      }
    };
  }

  public static JExpr zero() {
    return constantInt(0);
  }

  public static JExpr zero(final Type type) {
    return new ConstantValue(promoteSmallInts(type), 0);
  }

  private static JExpr binary(final int opcode, final JExpr x, final JExpr y, final Type resultType) {
    return new BinaryOpExpr(opcode, resultType, x, y);
  }

  private static Type promoteSmallInts(Type type) {
    switch (type.getSort()) {
      case BYTE:
      case BOOLEAN:
      case SHORT:
      case CHAR:
      case INT:
        return Type.INT_TYPE;
      default:
        return type;
    }
  }


  public static JExpr sum(JExpr x, int y) {
    if(y == 0) {
      return x;
    } else {
      return sum(x, constantInt(y));
    }
  }

  public static JExpr sum(final JExpr x, final JExpr y) {
    return binary(Opcodes.IADD, x, y, promoteSmallInts(x.getType()));
  }


  public static JExpr difference(JExpr x, JExpr y) {
    return binary(Opcodes.ISUB, x, y, promoteSmallInts(x.getType()));
  }

  public static JExpr difference(JExpr x, int y) {
    if(y == 0) {
      return x;
    } else {
      return difference(x, constantInt(y));
    }
  }

  public static JExpr product(JExpr x, JExpr y) {
    if(x instanceof ConstantValue && y instanceof ConstantValue) {
      return constantInt(
            ((ConstantValue) x).getIntValue() *
            ((ConstantValue) y).getIntValue());
    } else {
      return binary(Opcodes.IMUL, x, y, promoteSmallInts(x.getType()));
    }
  }
  
  public static JExpr product(JExpr x, int y) {
    if(y == 0) {
      return zero(x.getType());
    } else if(y == 1) {
      return x;
    } else {
      return product(x, new ConstantValue(x.getType(), y));
    }
  }

  public static JExpr divide(JExpr x, JExpr y) {
    return binary(Opcodes.IDIV, x, y, promoteSmallInts(x.getType()));
  }

  public static JExpr divide(JExpr size, int divisor) {
    Preconditions.checkArgument(size.getType().equals(Type.INT_TYPE));

    if(size instanceof ConstantValue) {
      return new ConstantValue(Type.INT_TYPE, ((ConstantValue) size).getIntValue() / divisor);
    }

    return divide(size, constantInt(divisor));
  }

  public static JLValue field(final JExpr instance, final Type fieldType, final String fieldName) {
    checkType("instance", instance, Type.OBJECT);
    
    return new JLValue() {
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
      public void store(MethodGenerator mv, JExpr value) {
        instance.load(mv);
        value.load(mv);
        mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
      }
    };
  }
  
  private static void checkType(String argName, JExpr value, int expectedSort) {
    if(value.getType().getSort() != expectedSort) {
      throw new IllegalArgumentException(String.format("Illegal type for %s: %s", argName, value.getType()));
    }
  }
  private static void checkType(String argName, JExpr value, Type expectedType) {
    if(!value.getType().equals(expectedType)) {
      throw new IllegalArgumentException(String.format("Illegal type %s for %s: Expected %s",
          value.getType(), argName, expectedType));
    }
  }

  public static JExpr cast(final JExpr object, final Type type) {
    
    // Can we reduce this to a NOOP ?
    if(object.getType().equals(type) || type.equals(Type.getType(Object.class))) {
      return object;
    }
    
    // Any Class[] to Object[] is also unnecessary
    if(object.getType().getSort() == Type.ARRAY && type.equals(Type.getType("[Ljava/lang/Object;"))) {
      return object;
    }
    
    // Verify that this is in the realm of possibility
    checkCast(object.getType(), type);

    return uncheckedCast(object, type);
  }
  
  public static JExpr uncheckedCast(final JExpr object, final Type type) {
    return new JLValue() {

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
      public void store(MethodGenerator mv, JExpr value) {
        if(!(object instanceof JLValue)) {
          throw new UnsupportedOperationException();
        }
        ((JLValue) object).store(mv, value);
      }
    };
  }

  public static JExpr castPrimitive(final JExpr expr, final Type type) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        expr.load(mv);
        mv.cast(expr.getType(), type);
      }
    };
  }

  private static void checkCast(Type fromType, Type toType) {

    if (fromType.equals(toType)) {
      return;
    }
    
    if (toType.getSort() != Type.OBJECT && 
        toType.getSort() != Type.ARRAY) {
      throw new IllegalArgumentException("Cannot cast from " + fromType + " to " + toType);
    }
    int fromSort = fromType.getSort();
    int toSort = toType.getSort();
 
    if(fromSort == Type.ARRAY) {
      if(toSort == Type.ARRAY) {
        checkCast(fromType.getElementType(), toType.getElementType());
      }
    }
  }

  public static JExpr voidValue() {
    return new JExpr() {
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

  public static JExpr thisValue(final Type type) {
    return new JExpr() {

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

  public static boolean isPrimitive(JExpr simpleExpr) {
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

  public static JExpr box(final JExpr simpleExpr) {
    Preconditions.checkArgument(isPrimitive(simpleExpr), "simpleExpr must be a primitive");
    
    Type primitiveType = simpleExpr.getType();
    final Type boxedType = boxedType(primitiveType);
    final String valueOfDescriptor = getMethodDescriptor(boxedType, primitiveType);
    
    return new JExpr() {
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

  public static JExpr copyOfArrayRange(final JExpr array, final JExpr from, final JExpr to) {

    final Type arrayType = array.getType();

    return new JExpr() {
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

  public static JExpr copyOfArray(final JExpr array) {

    final Type arrayType = array.getType();

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return arrayType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        // public static char[] copyOf(char[] original, int newLength) {

        array.load(mv);
        mv.dup();
        mv.arraylength();
        mv.invokestatic(Arrays.class, "copyOf",
            getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE));
      }
    };
  }
  public static JExpr newObject(final Class<?> classType, final JExpr... constructorArguments) {
    return newObject(Type.getType(classType), constructorArguments);
  }

  public static JExpr newObject(final Type classType, final JExpr... constructorArguments) {

    final Type argumentTypes[] = new Type[constructorArguments.length];
    for (int i = 0; i < constructorArguments.length; i++) {
      argumentTypes[i] = constructorArguments[i].getType();
    }

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return classType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.anew(classType);
        mv.dup();

        for (JExpr constructorArgument : constructorArguments) {
          constructorArgument.load(mv);
        }

        mv.invokeconstructor(classType, argumentTypes);
      }
    };
  }
  public static JExpr newObject(final Class classType, final String constructorDescriptor, final JExpr... constructorArguments) {
    return newObject(Type.getType(classType), constructorDescriptor, constructorArguments);
  }

  public static JExpr newObject(final Type classType, final String constructorDescriptor, final JExpr... constructorArguments) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return classType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.anew(classType);
        mv.dup();

        for (JExpr constructorArgument : constructorArguments) {
          constructorArgument.load(mv);
        }

        mv.invokespecial(classType.getInternalName(), "<init>", constructorDescriptor, false);
      }
    };
  }

  public static JExpr shiftRight(final JExpr x, int bits) {
    if(bits == 0) {
      return x;
    }
    return shiftRight(x, constantInt(bits));
  }
  
  public static JExpr shiftRight(final JExpr x, final JExpr bits) {
    return binary(Opcodes.ISHR, x, bits, promoteSmallInts(x.getType()));
  }

  public static JExpr shiftLeft(final JExpr x, final JExpr bits) {
    return binary(Opcodes.ISHL, x, bits, promoteSmallInts(x.getType()));
  }

  public static JExpr unsignedShiftRight(JExpr jexpr, JExpr bits) {
    return binary(Opcodes.IUSHR, jexpr, bits, promoteSmallInts(jexpr.getType()));
  }

  public static JLValue localVariable(final Type type, final int index) {
    return new JLValue() {

      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.load(index, type);
      }

      @Override
      public void store(MethodGenerator mv, JExpr rhs) {
        rhs.load(mv);
        mv.store(index, type);
      }
    };
  }

  public static JExpr identityHash(final JExpr value) {
    if (value.getType().getSort() != Type.OBJECT &&
        value.getType().getSort() != Type.ARRAY) {
      throw new IllegalArgumentException("value must have a reference type: " + value.getType());
    }
    
    return staticMethodCall(System.class, "identityHashCode", 
        getMethodDescriptor(Type.INT_TYPE, Type.getType(Object.class)), value);
  }
  
  public static JExpr numberOfLeadingZeros(JExpr value) {
    checkType("value", value, Type.INT_TYPE);
    
    return staticMethodCall(Type.getType(Integer.class), "numberOfLeadingZeros", 
        getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE), value);
  }

  public static JExpr methodCall(final JExpr instance,
                                 final Class declaringType,
                                 final String methodName,
                                 final String descriptor,
                                 final JExpr... arguments) {

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.getReturnType(descriptor);
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        instance.load(mv);
        for (JExpr argument : arguments) {
          argument.load(mv);
        }
        if(declaringType.isInterface()) {
          mv.invokeinterface(Type.getInternalName(declaringType), methodName, descriptor);
        } else {
          mv.invokevirtual(Type.getType(declaringType), methodName, descriptor, false);
        }
      }
    };
  }

  public static JExpr staticMethodCall(final Class declaringType, final String methodName,
                                       final String descriptor, final JExpr... arguments) {
    return staticMethodCall(Type.getType(declaringType), methodName, descriptor, arguments);
  }
  
  
  
  public static JExpr staticMethodCall(final Type declaringType, final String methodName, 
                                       final String descriptor, final JExpr... arguments) {

    if(Type.getArgumentTypes(descriptor).length != arguments.length) {
      throw new IllegalArgumentException(String.format("Call to %s.%s(%s) with incorrect number of arguments [%d]",
          declaringType.getInternalName(),
          methodName,
          Arrays.toString(Type.getArgumentTypes(descriptor)),
          arguments.length));
    }

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.getReturnType(descriptor);
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        for (JExpr argument : arguments) {
          argument.load(mv);
        }
        mv.invokestatic(declaringType, methodName, descriptor);
      }
    };
  }

  public static JLValue staticField(Field field) {
    if(!Modifier.isStatic(field.getModifiers())) {
      throw new IllegalArgumentException(field + " must be static");
    }
    Type declaringType = Type.getType(field.getDeclaringClass());
    Type fieldType = Type.getType(field.getType());
    String fieldName = field.getName();

    return staticField(declaringType, fieldName, fieldType);
  }
  
  public static JLValue staticField(final Type declaringType, final String fieldName, final Type fieldType) {
    
    if(declaringType.getSort() != Type.OBJECT) {
      throw new IllegalArgumentException(declaringType + " is not a class");
    }
    
    return new JLValue() {

      @Nonnull
      @Override
      public Type getType() {
        return fieldType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.getstatic(declaringType.getInternalName(), fieldName, fieldType.getDescriptor());
      }

      @Override
      public void store(MethodGenerator mv, JExpr expr) {
        expr.load(mv);
        mv.putstatic(declaringType.getInternalName(), fieldName, fieldType.getDescriptor());
      }
    };
  }

  public static JExpr bitwiseXor(JExpr x, int y) {
    return bitwiseXor(x, constantInt(y));
  }
  
  public static JExpr bitwiseXor(JExpr x, JExpr y) {
    return new BinaryOpExpr(Opcodes.IXOR, x, y);
  }
  
  public static JExpr flip(JExpr value) {
    switch (value.getType().getSort()) {
      case Type.BYTE:
        return bitwiseXor(value, constantInt(Byte.MIN_VALUE));
      case Type.SHORT:
        return bitwiseXor(value, constantInt(Short.MIN_VALUE));
      case Type.INT:
        return bitwiseXor(value, constantInt(Integer.MIN_VALUE));
      case Type.LONG:
        return bitwiseXor(value, constantLong(Long.MIN_VALUE));
      default:
        throw new UnsupportedOperationException("type: " + value.getType());
    }
  }

  public static JExpr constantClass(final Type valueType) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.getType(Class.class);
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.aconst(valueType);
      }
    };
  }

  public static JExpr max(final JExpr x, final JExpr y) {
    final Type type = x.getType();
    if(!type.equals(y.getType())) {
      throw new IllegalArgumentException(type + " != " + y.getType());
    }
    if(x instanceof ConstantValue && y instanceof ConstantValue) {
      if(type.equals(Type.INT_TYPE)) {
        return new ConstantValue(type, Math.max(((ConstantValue) x).getIntValue(), ((ConstantValue) y).getIntValue()));
      }
    }
    
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        x.load(mv);
        y.load(mv);
        mv.invokestatic(Math.class, "max", Type.getMethodDescriptor(type, type, type));
      }
    };
  }

  public static boolean requiresCast(Type fromType, Type toType) {
    if (fromType.equals(toType)) {
      return false;
    }

    if (promoteSmallInts(fromType).equals(promoteSmallInts(toType))) {
      return false;
    }

    if(toType.equals(Type.getType(Object.class))) {
      if(fromType.getSort() == ARRAY || fromType.getSort() == OBJECT) {
        return false;
      }
    }

    if(fromType.getSort() == Type.OBJECT && toType.getSort() == Type.OBJECT) {
      return !isDefinitelySubclass(fromType, toType);
    }

    if(fromType.getSort() == Type.ARRAY && toType.getSort() == Type.ARRAY) {
      return true;
    }

    throw new IllegalStateException(fromType + " will never be assignable to " + toType);
  }

  private static boolean isDefinitelySubclass(Type fromType, Type toType) {
    Class fromClass;
    try {
      fromClass = Class.forName(fromType.getClassName());
    } catch (ClassNotFoundException e) {
      return false;
    }

    Class toClass;
    try {
      toClass = Class.forName(toType.getClassName());
    } catch (ClassNotFoundException e) {
      return false;
    }

    return toClass.isAssignableFrom(fromClass);
  }

  /**
   * Truncates and sign-extends a signed 32-bit integer.
   */
  public static JExpr i2b(final JExpr expr) {
    return unaryOp(expr, Type.INT_TYPE, Opcodes.I2B, Type.BYTE_TYPE);
  }

  public static JExpr i2c(JExpr jexpr) {
    return unaryOp(jexpr, Type.INT_TYPE, Opcodes.I2C, Type.CHAR_TYPE);
  }


  /**
   * Truncates and sign-extends a signed 32-bit integer.
   */
  public static JExpr i2s(final JExpr expr) {
    return unaryOp(expr, Type.INT_TYPE, Opcodes.I2S, Type.SHORT_TYPE);
  }

  public static JExpr f2d(final JExpr expr) {
    return unaryOp(expr, Type.FLOAT_TYPE, Opcodes.F2D, Type.DOUBLE_TYPE);
  }

  public static JExpr d2f(final JExpr expr) {
    return unaryOp(expr, Type.DOUBLE_TYPE, Opcodes.D2F, Type.FLOAT_TYPE);
  }

  public static JExpr d2i(final JExpr expr) {
    return unaryOp(expr, Type.DOUBLE_TYPE, Opcodes.D2I, Type.INT_TYPE);
  }

  public static JExpr d2l(final JExpr expr) {
    return unaryOp(expr, Type.DOUBLE_TYPE, Opcodes.D2L, Type.LONG_TYPE);
  }

  public static JExpr f2i(final JExpr expr) {
    return unaryOp(expr, Type.FLOAT_TYPE, Opcodes.F2I, Type.INT_TYPE);
  }
  public static JExpr f2l(final JExpr expr) {
    return unaryOp(expr, Type.FLOAT_TYPE, Opcodes.F2L, Type.LONG_TYPE);
  }


  public static JExpr l2d(JExpr jexpr) {
    return unaryOp(jexpr, Type.LONG_TYPE, Opcodes.L2D, Type.DOUBLE_TYPE);
  }
  public static JExpr l2f(JExpr jexpr) {
    return unaryOp(jexpr, Type.LONG_TYPE, Opcodes.L2F, Type.FLOAT_TYPE);
  }

  public static JExpr l2i(JExpr jexpr) {
    return unaryOp(jexpr, Type.LONG_TYPE, Opcodes.L2I, Type.INT_TYPE);
  }



  private static JExpr unaryOp(final JExpr expr, Type expectedArgumentType, final int opcode, final Type resultType) {
    assert promoteSmallInts(expr.getType()).equals(expectedArgumentType) :
        "Expected " + expectedArgumentType + ", found: " + expr.getType();
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return resultType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        expr.load(mv);
        mv.visitInsn(opcode);
      }
    };
  }

  public static JExpr remainder(JExpr x, JExpr y) {
    return binary(Opcodes.IREM, x, y, promoteSmallInts(x.getType()));
  }


  public static JExpr negative(final JExpr jexpr) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return promoteSmallInts(jexpr.getType());
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        jexpr.load(mv);
        mv.visitInsn(jexpr.getType().getOpcode(Opcodes.INEG));
      }
    };
  }

  public static JExpr i2f(JExpr jexpr) {
    return unaryOp(jexpr, Type.INT_TYPE, Opcodes.I2F, Type.FLOAT_TYPE);
  }

  public static JExpr i2l(JExpr jexpr) {
    return unaryOp(jexpr, Type.INT_TYPE, Opcodes.I2L, Type.LONG_TYPE);
  }

  public static JExpr i2d(JExpr jexpr) {
    return unaryOp(jexpr, Type.INT_TYPE, Opcodes.I2D, Type.DOUBLE_TYPE);
  }


  public static JExpr bitwiseAnd(JExpr expr, int mask) {
    return bitwiseAnd(expr, constantInt(mask));
  }

  public static JExpr bitwiseOr(JExpr x, JExpr y) {
    return bitwiseOp(Opcodes.IOR, x, y);
  }

  public static JExpr bitwiseAnd(JExpr x, JExpr y) {
    return bitwiseOp(Opcodes.IAND, x, y);
  }

  private static JExpr bitwiseOp(final int opcode, final JExpr x, final JExpr y) {

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return promoteSmallInts(x.getType());
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        x.load(mv);
        y.load(mv);
        mv.visitInsn(promoteSmallInts(x.getType()).getOpcode(opcode));
      }
    };
  }

  public static JExpr lcmp(final JExpr x, final JExpr y) {
    assert x.getType().equals(Type.LONG_TYPE);
    assert y.getType().equals(Type.LONG_TYPE);

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.INT_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        x.load(mv);
        y.load(mv);
        mv.visitInsn(Opcodes.LCMP);
      }
    };
  }

  public static JExpr cmpg(JExpr x, JExpr y) {
    assert x.getType().equals(y.getType());
    switch (x.getType().getSort()) {
      case FLOAT:
        return floatingPointComparison(Opcodes.FCMPG, x, y);
      case DOUBLE:
        return floatingPointComparison(Opcodes.DCMPG, x, y);
    }
    throw new UnsupportedOperationException("type: " + x.getType());
  }


  public static JExpr cmpl(JExpr x, JExpr y) {
    assert x.getType().equals(y.getType());
    switch (x.getType().getSort()) {
      case FLOAT:
        return floatingPointComparison(Opcodes.FCMPL, x, y);
      case DOUBLE:
        return floatingPointComparison(Opcodes.DCMPL, x, y);
    }
    throw new UnsupportedOperationException("type: " + x.getType());
  }





  public static JExpr floatingPointComparison(final int opcode, final JExpr x, final JExpr y) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.INT_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        x.load(mv);
        y.load(mv);
        mv.visitInsn(opcode);
      }
    };
  }

  public static JExpr compareTo(JExpr x, JExpr y) {
    return methodCall(x, Comparable.class, "compareTo",
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(Object.class)), y);
  }

  public static JExpr objectEquals(JExpr x, JExpr y) {
    return methodCall(x, Object.class, "equals",
        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class)), y);
  }

  public static JExpr constantFloat(final float v) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.FLOAT_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.fconst(v);
      }
    };
  }

  public static JExpr constantDouble(final double v) {
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.DOUBLE_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.dconst(v);
      }
    };
  }

}