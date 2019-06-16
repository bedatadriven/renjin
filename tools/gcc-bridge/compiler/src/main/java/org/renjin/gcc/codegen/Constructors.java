package org.renjin.gcc.codegen;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleStringConstant;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.hash.Hashing;

import javax.annotation.Nonnull;
import java.io.IOException;

public class Constructors {


  public static VArrayExpr isStringArray(MethodGenerator mv, ResourceWriter resourceWriter, GimpleConstructor value) {

    // Check to see if this uberhaupt an array of byte pointers
    if(!isArrayWithType(value.getType(),
          c -> isPointerWithType(c,
              cc -> isIntegerWithPrecision(cc, 8)))) {
      return null;
    }

    GimpleArrayType arrayType = (GimpleArrayType) value.getType();

    // Concatenate all the strings together, ensuring that they are all null terminated, and have
    // no internal nulls

    StringBuilder stringBuilder = new StringBuilder();
    for (GimpleConstructor.Element element : value.getElements()) {

      if(!(element.getValue() instanceof GimpleAddressOf)) {
        return null;
      }
      GimpleAddressOf addressOf = (GimpleAddressOf) element.getValue();
      if(!(addressOf.getValue() instanceof GimpleStringConstant)) {
        return null;
      }
      GimpleStringConstant stringConstant = (GimpleStringConstant) addressOf.getValue();
      if (!isNullTerminated(stringConstant)) {
        return null;
      }

      stringBuilder.append(stringConstant.getValue());
    }

    if(stringBuilder.length() < 1000) {

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

    LocalVarAllocator.LocalVar var = mv.getLocalVarAllocator().reserve(Type.getType(Ptr.class));

    mv.store(var.getIndex(), Type.getType(Ptr.class));

    return new VArrayExpr(arrayType, new VPtrExpr(var));
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

  private static boolean isIntegerWithPrecision(GimpleType type, int precision) {
    if(!(type instanceof GimpleIntegerType)) {
      return false;
    }

    GimpleIntegerType integerComponentType = (GimpleIntegerType) type;

    return integerComponentType.getPrecision() == precision;
  }

  private static boolean isNullTerminated(GimpleStringConstant stringConstant) {
    int nullPosition = stringConstant.getValue().indexOf('\0');
    return nullPosition == stringConstant.getValue().length() - 1;
  }

  public static GExpr isCharArray(MethodGenerator mv, ResourceWriter resourceWriter, GimpleConstructor expr) {

    if(!isArrayWithType(expr.getType(), c -> isIntegerWithPrecision(c, 16))) {
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

    return new VArrayExpr((GimpleArrayType)expr.getType(), new VPtrExpr(array));
  }
}
