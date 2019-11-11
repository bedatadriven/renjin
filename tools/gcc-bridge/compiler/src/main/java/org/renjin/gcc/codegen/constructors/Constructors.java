package org.renjin.gcc.codegen.constructors;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.ResourceWriter;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.var.LocalVarAllocator.LocalVar;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.hash.Hashing;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

class Constructors {

  interface ConstructorInterface {
    GExpr tryCreate(
        MethodGenerator mv,
        ResourceWriter resourceWriter,
        GimpleConstructor value
    );
  }

  interface BufferConverter {
    void append(ByteBuffer buffer, GimpleConstructor.Element element);
  }

  /**
   * Constructors for circumventing 'Class too large' and 'Method too large' errors when arrays hold aggregate
   * initialisers of very large size.
   *
   * These errors occurs as the size of any method or class cannot exceed the addressing limit of 16-bits for it's
   * 'jump' commands.  Aggregate initialisation is translated by gcc-bridge into inline code that can exceed these
   * limits for very large arrays.  The mitigation is to export these large arrays to an inline resource which is then
   * streamed into the array at construction.
   */

  static ConstructorInterface largeShortArray = (mv, resourceWriter, value) -> {
    Predicate<GimpleType> isElementType = c -> isIntegerWithPrecision(c, 16);
    BufferConverter converter = (buffer, element) -> {
      GimpleIntegerConstant constant = (GimpleIntegerConstant)element.getValue();
      short s = constant.getValue().shortValue();
      buffer.putShort(s);
    };

    ByteBuffer buffer = constantArrayToBuffer(value, isElementType, converter, 2);
    if (buffer == null) return null;
    LocalVar var = injectLoadableResource(mv, resourceWriter, buffer, "shortArrayFromResource");
    GimpleArrayType arrayType = (GimpleArrayType) value.getType();
    return new VArrayExpr(arrayType, new VPtrExpr(var));
  };

  static ConstructorInterface largeDoubleArray = (mv, resourceWriter, value) -> {
    Predicate<GimpleType> isElementType = c -> isRealWithPrecision(c, 64);
    BufferConverter converter = (buffer, element) -> {
      GimpleRealConstant constant = (GimpleRealConstant)element.getValue();
      double d = constant.getValue();
      buffer.putDouble(d);
    };

    ByteBuffer buffer = constantArrayToBuffer(value, isElementType, converter, 8);
    if (buffer == null) return null;
    LocalVar var = injectLoadableResource(mv, resourceWriter, buffer, "doubleArrayFromResource");
    GimpleArrayType arrayType = (GimpleArrayType) value.getType();
    return new VArrayExpr(arrayType, new VPtrExpr(var));
  };

  /**
   * Specialised constructors for handling large string arrays
   */

  static ConstructorInterface stringArray = (mv, resourceWriter, value) -> {
    // Check to see if this uberhaupt an array of byte pointers
    Predicate<GimpleType> isElementType = c -> isIntegerWithPrecision(c, 8);
    boolean isArrayType = isArrayWithType(value.getType(), c -> isPointerWithType(c, isElementType));
    if (!isArrayType) {
      return null;
    }

    GimpleArrayType arrayType = (GimpleArrayType) value.getType();

    // Concatenate all the strings together, ensuring that they are all null terminated, and have
    // no internal nulls

    StringBuilder stringBuilder = new StringBuilder();
    for (GimpleConstructor.Element element : value.getElements()) {

      if (!(element.getValue() instanceof GimpleAddressOf)) {
        return null;
      }
      GimpleAddressOf addressOf = (GimpleAddressOf) element.getValue();
      if (!(addressOf.getValue() instanceof GimpleStringConstant)) {
        return null;
      }
      GimpleStringConstant stringConstant = (GimpleStringConstant) addressOf.getValue();
      if (!isNullTerminated(stringConstant)) {
        return null;
      }

      stringBuilder.append(stringConstant.getValue());
    }

    if (stringBuilder.length() < 1000) {

      // Store the concatenated string in the Class' string pool

      mv.aconst(stringBuilder.toString());
      mv.invokestatic(Type.getType(BytePtr.class), "stringArray",
          Type.getMethodDescriptor(Type.getType(Ptr.class), Type.getType(String.class)));

    } else {

      // Store in an external resource

      byte[] bytes = stringBuilder.toString().getBytes(Charsets.US_ASCII);
      String resourceName = Hashing.md5().hashBytes(bytes).toString();

      try {
        resourceWriter.writeResource(resourceName, bytes);
      } catch (IOException e) {
        throw new InternalCompilerException("Failed to write resource", e);
      }

      mv.aconst(mv.getOwnerClass());
      mv.aconst(resourceName);
      mv.invokestatic(Type.getType(BytePtr.class), "stringArrayFromResource",
          Type.getMethodDescriptor(Type.getType(Ptr.class), Type.getType(Class.class), Type.getType(String.class)));
    }

    // Save the array to a local variable

    LocalVar var = mv.getLocalVarAllocator().reserve(Type.getType(Ptr.class));

    mv.store(var.getIndex(), Type.getType(Ptr.class));

    return new VArrayExpr(arrayType, new VPtrExpr(var));
  };

  static ConstructorInterface charArray = (mv, resourceWriter, expr) -> {

    if (!isArrayWithType(expr.getType(), c -> isIntegerWithPrecision(c, 16))) {
      return null;
    }

    StringBuilder s = new StringBuilder();
    for (GimpleConstructor.Element element : expr.getElements()) {
      GimpleIntegerConstant constant = (GimpleIntegerConstant) element.getValue();
      s.appendCodePoint(constant.getNumberValue().intValue());
    }

    JExpr array = new JExpr() {

      @Nonnull
      @Override
      public Type getType() {
        return Type.getType(Ptr.class);
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.aconst(s.toString());
        mv.invokestatic(Type.getType(CharPtr.class), "int16Array",
            Type.getMethodDescriptor(Type.getType(Ptr.class),
                Type.getType(String.class)));

      }
    };

    return new VArrayExpr((GimpleArrayType) expr.getType(), new VPtrExpr(array));
  };

  /* Convenience functions */

  private static ByteBuffer constantArrayToBuffer(
      GimpleConstructor value,
      Predicate<GimpleType> isElementType,
      BufferConverter converter,
      int byteSize
  ) {
    boolean isArrayType = isArrayWithType(value.getType(), isElementType);
    if (!isArrayType) {
      return null;
    }
    List<GimpleConstructor.Element> elements = value.getElements();
    if (elements.size() < 4000) { // only stream large arrays
      return null;
    }
    ByteBuffer buffer = ByteBuffer.allocate(elements.size() * byteSize).order(ByteOrder.LITTLE_ENDIAN);
    for (GimpleConstructor.Element element : elements) {
      converter.append(buffer, element);
    }
    return buffer;
  }

  private static LocalVar injectLoadableResource(
      MethodGenerator mv,
      ResourceWriter resourceWriter,
      ByteBuffer buffer,
      String functionName
  ) {
    // Write resource to class
    byte[] bytes = buffer.array();
    String resourceName = Hashing.md5().hashBytes(bytes).toString();
    try {
      resourceWriter.writeResource(resourceName, bytes);
    } catch (IOException e) {
      throw new InternalCompilerException("Failed to write resource", e);
    }

    // Inject call to initialisation function to parse resource from class
    mv.aconst(mv.getOwnerClass());
    mv.aconst(resourceName);
    String descriptor = Type.getMethodDescriptor(
        Type.getType(Ptr.class),
        Type.getType(Class.class),
        Type.getType(String.class)
    );
    mv.invokestatic(Type.getType(BytePtr.class), functionName, descriptor);

    // Save the array to a local variable
    LocalVar var = mv.getLocalVarAllocator().reserve(Type.getType(Ptr.class));
    mv.store(var.getIndex(), Type.getType(Ptr.class));
    return var;
  }

  private static boolean isArrayWithType(GimpleType type, Predicate<GimpleType> componentTypePredicate) {
    if(!(type instanceof GimpleArrayType)) {
      return false;
    }

    GimpleArrayType arrayType = (GimpleArrayType) type;

    return componentTypePredicate.apply(arrayType.getComponentType());
  }

  private static boolean isPointerWithType(GimpleType type, Predicate<GimpleType> baseTypePredicate) {
    if(!(type instanceof GimplePointerType)) {
      return false;
    }

    GimplePointerType pointerType = (GimplePointerType) type;

    return baseTypePredicate.apply(pointerType.getBaseType());
  }

  private static boolean isNullTerminated(GimpleStringConstant stringConstant) {
    int nullPosition = stringConstant.getValue().indexOf('\0');
    return nullPosition == stringConstant.getValue().length() - 1;
  }

  /* Number type inspection */

  private static GimpleIntegerType getIntegerType(GimpleType type) {
    boolean isInteger = type instanceof GimpleIntegerType;
    if(!isInteger) return null;
    return (GimpleIntegerType)type;
  }

  private static boolean isIntegerWithPrecision(GimpleType type, int precision) {
    GimpleIntegerType integerComponentType = getIntegerType(type);
    return (integerComponentType != null) && integerComponentType.getPrecision() == precision;
  }

  private static GimpleRealType getRealType(GimpleType type) {
    boolean isReal = type instanceof GimpleRealType;
    if(!isReal) return null;
    return (GimpleRealType)type;
  }

  private static boolean isRealWithPrecision(GimpleType type, int precision) {
    GimpleRealType realComponentType = getRealType(type);
    return (realComponentType != null) && realComponentType.getPrecision() == precision;
  }

}
