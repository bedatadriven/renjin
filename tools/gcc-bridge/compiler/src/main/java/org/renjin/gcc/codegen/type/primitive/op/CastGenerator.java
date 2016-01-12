package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Generates the bytecode to cast a primitive value to a new type
 */
public class CastGenerator extends AbstractExprGenerator implements ExprGenerator {
  
  private ExprGenerator valueGenerator;
  private GimplePrimitiveType destinationType;

  public CastGenerator(ExprGenerator valueGenerator, GimplePrimitiveType destinationType) {
    this.valueGenerator = valueGenerator;
    this.destinationType = destinationType;
  }

  @Override
  public GimpleType getGimpleType() {
    return destinationType;
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {

    valueGenerator.emitPrimitiveValue(mv);
    
    if(!valueGenerator.getGimpleType().equals(destinationType)) {
      cast(mv, (GimplePrimitiveType) valueGenerator.getGimpleType(), destinationType);
    }
  }
  
  private void cast(MethodVisitor mv, GimplePrimitiveType sourceType, GimplePrimitiveType destinationType) {
    // Use reflection to do poor man's case statement
    String methodName = "cast" + castSignature(sourceType) + "To" + castSignature(destinationType);
    Method castMethod;
    try {
      castMethod = getClass().getMethod(methodName, MethodVisitor.class);
    } catch (NoSuchMethodException e) {
      throw new UnsupportedOperationException("Unsupported cast: " + methodName);
    }

    try {
      castMethod.invoke(null, mv);
    } catch (IllegalAccessException e) {
      throw new InternalCompilerException("Exception invoking " + methodName, e);
    } catch (InvocationTargetException e) {
      throw new InternalCompilerException("Exception invoking " + methodName, e.getCause());
    }
  }

  private String castSignature(GimplePrimitiveType type) {
    if(type instanceof GimpleIntegerType) {

      GimpleIntegerType intType = (GimpleIntegerType) type;
      int precision = intType.getPrecision();
      
      if(precision == 0) {
        throw new AssertionError("precision == 0");
      }

      if(intType.isUnsigned()) {
        return "UnsignedInt" + precision;
      } else {
        return "Int" + precision;
      }
    } else if(type instanceof GimpleRealType) {
      return "Real" + ((GimpleRealType) type).getPrecision();
      
    } else if(type instanceof GimpleBooleanType) {
      return "Bool";
      
    } else {
      throw new IllegalArgumentException("type: " + type);
    }
  }


  /**
   * Most of the time there is no need to use larger numeric types for simulating unsigned types in Java. 
   *
   * For addition, subtraction, multiplication, shift left, the logical operations, equality and casting to a smaller numeric type it doesn't matter whether the operands are signed or unsigned, the result will be the same regardless, viewed as a bit pattern.

   For signed casting to a larger type just do it.

   For unsigned casting from a smaller type to a long use & with a mask of type long for the smaller type. E.g., short to long: s & 0xffffL.

   For unsigned casting from a smaller type to an int use & with a mask of type int. E.g., byte to int: b & 0xff.

   Otherwise do like in the int case and apply a cast on top. E.g., byte to short: (short) (b & 0xff).

   For the comparison operators < etc. and division the easiest is to cast to a larger type and do the operation there. But there also exist other options, e.g. do comparisons after adding an appropriate offset.
   */

  /** From BOOL  **/
   
  public static void castBoolToInt8(MethodVisitor mv) {
    // NOOP
    // Same representation on stack
  }

  public static void castBoolToInt16(MethodVisitor mv) {
    // NOOP
    // Same representation on stack
  }

  public static void castBoolToInt32(MethodVisitor mv) {
    // NOOP
    // Same representation on stack
  }

  public static void castBoolToInt64(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2L);
  }
  
  

  /** From Int8 (byte) **/
  
  public static void castInt8ToBool(MethodVisitor mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castInt8ToUnsignedInt8(MethodVisitor mv) { 
    // NOOP
    // same bitwise representation
  }
  
  public static void castInt8ToInt16(MethodVisitor mv) { 
    // NOOP
    // same bitwise representation
  }
  
  public static void castInt8ToUnsignedInt16(MethodVisitor mv) { 
    mv.visitLdcInsn(-1 >>> 16);
    mv.visitInsn(Opcodes.IAND);
  }
  
  public static void castInt8ToInt32(MethodVisitor mv) { 
    // NOOP
    // operand will always be less than < Integer.MAX_VALUE
    // so signed/unsigned representation is the same
  }
  public static void castInt8ToUnsignedInt32(MethodVisitor mv) { 
    // NOOP
    mv.visitLdcInsn(-1 >>> 32);
    mv.visitInsn(Opcodes.IAND);
  }
  
  public static void castInt8ToInt64(MethodVisitor mv) {
    // operand will always be less than < Long.MAX_VALUE
    // so signed/unsigned representation is the same
    mv.visitInsn(Opcodes.I2L);
  }
  
  public static void castInt8ToUnsignedInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2L);

  }
  
  public static void castInt8ToReal32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  public static void castInt8ToReal64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2D);
  }
  
  
  /*** FROM UNSIGNED INT 8 (Unsigned byte) **/

  public static void castUnsignedInt8ToBool(MethodVisitor mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castUnsignedInt8ToInt8(MethodVisitor mv) { 
    // NOOP 
    // same bitwise pattern
  }

  public static void castUnsignedInt8ToInt(MethodVisitor mv) {
    mv.visitIntInsn(Opcodes.BIPUSH, 255);
    mv.visitInsn(Opcodes.IAND);
  }
  
  public static void castUnsignedInt8ToInt16(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
  }
  public static void castUnsignedInt8ToUnsignedInt16(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
  }
  
  public static void castUnsignedInt8ToInt32(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
  }
  
  public static void castUnsignedInt8ToUnsignedInt32(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
  }
  public static void castUnsignedInt8ToInt64(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2L);
  }
  public static void castUnsignedInt8ToUnsignedInt64(MethodVisitor mv) {
    // operand is always < Long.MAX_VALUE
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2L);
  }
  public static void castUnsignedInt8ToReal32(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2F);
  }
  
  public static void castUnsignedInt8ToReal64(MethodVisitor mv) {
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2D);
  }

  /** FROM SIGNED INT 16 (short) **/
  
  public static void castInt16ToBool(MethodVisitor mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castInt16ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt16ToUnsignedInt8(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2B);
  }

  public static void castInt16ToUnsignedInt16(MethodVisitor mv) { 
    // NOOP 
    // Same bitwise representation
  }
  
  public static void castInt16ToInt32(MethodVisitor mv) { 
    // NOOP
    // Signed short and int have same representation on stack
  }
  
  public static void castInt16ToUnsignedInt32(MethodVisitor mv) {
    // NOOP
    // Signed short and int have same representation on stack
  }
  public static void castInt16ToInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2L);
  }
  public static void castInt16ToUnsignedInt64(MethodVisitor mv) {
    // operand will always be < Long.MAX_VALUE
    // so signed/unsigned representations will bitwise equivalent
    mv.visitInsn(Opcodes.I2L);
    
  }
  public static void castInt16ToReal32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  
  public static void castInt16ToReal64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2D);
  }
  
  
  /** UNSIGNED INT (char) **/

  public static void castUnsignedInt16ToBool(MethodVisitor mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castUnsignedInt16ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  public static void castUnsignedInt16ToUnsignedInt8(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castUnsignedInt16ToInt16(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2S); 
  }
  public static void castUnsignedInt16ToInt32(MethodVisitor mv) {
    // NOOP
    // same bitwise representation
  }
  
  public static void castUnsignedInt16ToUnsignedInt32(MethodVisitor mv) { 
    // NOOP
    // Same bitwise representation
  }
  
  public static void castUnsignedInt16ToInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2L);
  }
  
  public static void castUnsignedInt16ToUnsignedInt64(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2L);
  }
  
  public static void castUnsignedInt16ToReal32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  public static void castUnsignedInt16ToReal64(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2D);
  }

  
  /** SIGNED INT 32 (int) **/
  
  public static void castInt32ToBool(MethodVisitor mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castInt32ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt32ToUnsignedInt8(MethodVisitor mv) { 
    throw new UnsupportedOperationException();
  }
  
  public static void castInt32ToInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2S);
  }

  public static void castInt32ToUnsignedInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2C);
  }

  public static void castInt32ToInt32(MethodVisitor mv) {
    // NOOP
    // Only present because strictly speaking enumerated and integer types are not considered
    // to be exactly the same type
  }

  public static void castInt32ToUnsignedInt32(MethodVisitor mv) { 
    // NOOP 
    // same bitwise representation
  }
  
  public static void castInt32ToInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2L);
  }

  public static void castInt32ToUnsignedInt64(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  
  public static void castInt32ToReal32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  public static void castInt32ToReal64(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2D);
  }
  
  
  
  
  /** FROM UNSIGNED INT 32 */
  
  public static void castUnsignedInt32ToBool(MethodVisitor mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castUnsignedInt32ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castUnsignedInt32ToUnsignedInt8(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  
  public static void castUnsignedInt32ToInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2S);
  }
  
  public static void castUnsignedInt32ToUnsignedInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.I2C);
  }
  
  public static void castUnsignedInt32ToInt32(MethodVisitor mv) { 
    // NOOP
    // Bitwise representations are the same
  }
  
  public static void castUnsignedInt32ToInt64(MethodVisitor mv) {
    mv.visitInsn(Opcodes.I2L);
    mv.visitLdcInsn(0xffffffffL);
    mv.visitInsn(Opcodes.LAND);
  }
  
  public static void castUnsignedInt32ToUnsignedInt64(MethodVisitor mv) { 
    // operand will always be < Long.MAX value
    // so signed/unsigned representation will be the same
    castUnsignedInt32ToInt64(mv);
  }
  
  public static void castUnsignedInt32ToReal32(MethodVisitor mv) {
    castUnsignedInt32ToInt64(mv);
    mv.visitInsn(Opcodes.L2D);
  }
  
  public static void castUnsignedInt32ToReal64(MethodVisitor mv) {
    castUnsignedInt32ToInt64(mv);
    mv.visitInsn(Opcodes.L2D);
  }

  /* FROM INT 64 (long) */
  
  public static void castInt64ToBool(MethodVisitor mv) {
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castInt64ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.L2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt64ToUnsignedInt8(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
 
  public static void castInt64ToInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.L2I);
    mv.visitInsn(Opcodes.I2S);
  }
  
  public static void castInt64ToUnsignedInt16(MethodVisitor mv) { 
    throw new UnsupportedOperationException();
  }
  
  public static void castInt64ToInt32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.L2I);
  }

  public static void castInt64ToUnsignedInt32(MethodVisitor mv) {
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castInt64ToUnsignedInt64(MethodVisitor mv) { 
    // NOOP
    // Bitwise representations are the same
  }
    
  public static void castInt64ToReal32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.L2F);
  }
  
  public static void castInt64ToReal64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.L2D);
  }

  /* FROM UNSIGNED INT 64 (longish) */
   
  public static void castUnsignedInt64ToBool(MethodVisitor mv) {
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castUnsignedInt64ToInt8(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToUnsignedInt8(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToInt16(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToUnsignedInt16(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToInt32(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToUnsignedInt32(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToInt64(MethodVisitor mv) {    
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToReal32(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToReal64(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  /** FROM REAL 32 (float) */
  
  public static void castReal32ToBool(MethodVisitor mv) {
    mv.visitInsn(Opcodes.F2I);
  }
  
  public static void castReal32ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castReal32ToUnsignedInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castReal32ToInt16(MethodVisitor mv) {
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2S);
  }
  
  public static void castReal32ToUnsignedInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2C);
  }
  
  public static void castReal32ToInt32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2I);
  }
  
  public static void castReal32ToUnsignedInt32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2L);
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castReal32ToInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2L);
  }
  
  public static void castReal32ToUnsignedInt64(MethodVisitor mv) {
    mv.visitInsn(Opcodes.F2L);
  }
  
  public static void castReal32ToReal64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.F2D);
  }


  /** FROM REAL 64 (double) */
  
  public static void castReal64ToBool(MethodVisitor mv) {
    mv.visitInsn(Opcodes.D2I);
  }
  
  public static void castReal64ToInt8(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castReal64ToUnsignedInt8(MethodVisitor mv) {
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2B);
  }

  public static void castReal64ToInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2S);
  }

  public static void castReal64ToUnsignedInt16(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2C);
  }
  
  public static void castReal64ToInt32(MethodVisitor mv) {
    mv.visitInsn(Opcodes.D2L);
    mv.visitInsn(Opcodes.L2I);
  }
  
  
  public static void castReal64ToUnsignedInt32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2L);
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castReal64ToInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2L);
  }
  
  public static void castReal64ToUnsignedInt64(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2L);
  }
  
  public static void castReal64ToReal32(MethodVisitor mv) { 
    mv.visitInsn(Opcodes.D2F);
  }

}
