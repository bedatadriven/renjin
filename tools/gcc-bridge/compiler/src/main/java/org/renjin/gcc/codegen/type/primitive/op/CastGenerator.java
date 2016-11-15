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
package org.renjin.gcc.codegen.type.primitive.op;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Generates the bytecode to cast a primitive value to a new type
 */
@SuppressWarnings("unused")
public class CastGenerator implements JExpr {
  
  private JExpr valueGenerator;
  private GimplePrimitiveType sourceType;
  private GimplePrimitiveType destinationType;

  public CastGenerator(JExpr valueGenerator, GimplePrimitiveType sourceType, GimplePrimitiveType destinationType) {
    this.valueGenerator = valueGenerator;
    this.sourceType = sourceType;
    this.destinationType = destinationType;
  }


  @Nonnull
  @Override
  public Type getType() {
    return destinationType.jvmType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    valueGenerator.load(mv);
    
    if(!sourceType.equals(destinationType)) {
      cast(mv, this.sourceType, destinationType);
    }
  }
  
  private void cast(MethodGenerator mv, GimplePrimitiveType sourceType, GimplePrimitiveType destinationType) {
    // Use reflection to do poor man's case statement
    String methodName = "cast" + castSignature(sourceType) + "To" + castSignature(destinationType);
    Method castMethod;
    try {
      castMethod = getClass().getMethod(methodName, MethodGenerator.class);
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
      int precision = ((GimpleRealType) type).getPrecision();
      if(precision <= 32) {
        precision = 32;
      } else {
        precision = 64;
      }
      return "Real" + precision;
      
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
   
  public static void castBoolToInt8(MethodGenerator mv) {
    // NOOP
    // Same representation on stack
  }

  public static void castBoolToInt16(MethodGenerator mv) {
    // NOOP
    // Same representation on stack
  }

  public static void castBoolToInt32(MethodGenerator mv) {
    // NOOP
    // Same representation on stack
  }

  public static void castBoolToInt64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2L);
  }

  public static void castBoolToUnsignedInt8(MethodGenerator mv) {
    // NOOP
  }

  public static void castBoolToUnsignedInt16(MethodGenerator mv) {
    // NOOP
  }


  public static void castBoolToUnsignedInt32(MethodGenerator mv) {
    // NOOP
  }

  public static void castBoolToUnsignedInt64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2L);
  }

  public static void castBoolToReal32(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2F);
  }

  public static void castBoolToReal64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2D);
  }

  /** From Int8 (byte) **/
  
  public static void castInt8ToBool(MethodGenerator mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castInt8ToUnsignedInt8(MethodGenerator mv) { 
    // NOOP
    // same bitwise representation
  }
  
  public static void castInt8ToInt16(MethodGenerator mv) { 
    // NOOP
    // same bitwise representation
  }
  
  public static void castInt8ToUnsignedInt16(MethodGenerator mv) { 
    mv.visitLdcInsn(-1 >>> 16);
    mv.visitInsn(Opcodes.IAND);
  }
  
  public static void castInt8ToInt32(MethodGenerator mv) { 
    // NOOP
    // operand will always be less than < Integer.MAX_VALUE
    // so signed/unsigned representation is the same
  }
  public static void castInt8ToUnsignedInt32(MethodGenerator mv) { 
    // NOOP
    mv.visitLdcInsn(-1 >>> 32);
    mv.visitInsn(Opcodes.IAND);
  }
  
  public static void castInt8ToInt64(MethodGenerator mv) {
    // operand will always be less than < Long.MAX_VALUE
    // so signed/unsigned representation is the same
    mv.visitInsn(Opcodes.I2L);
  }
  
  public static void castInt8ToUnsignedInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2L);

  }
  
  public static void castInt8ToReal32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  public static void castInt8ToReal64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2D);
  }
  
  
  /*** FROM UNSIGNED INT 8 (Unsigned byte) **/

  public static void castUnsignedInt8ToBool(MethodGenerator mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castUnsignedInt8ToInt8(MethodGenerator mv) { 
    // NOOP 
    // same bitwise pattern
  }

  public static void castUnsignedInt8ToInt(MethodGenerator mv) {
    mv.visitIntInsn(Opcodes.BIPUSH, 255);
    mv.visitInsn(Opcodes.IAND);
  }
  
  public static void castUnsignedInt8ToInt16(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
  }
  public static void castUnsignedInt8ToUnsignedInt16(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
  }
  
  public static void castUnsignedInt8ToInt32(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
  }
  
  public static void castUnsignedInt8ToUnsignedInt32(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
  }
  public static void castUnsignedInt8ToInt64(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2L);
  }
  public static void castUnsignedInt8ToUnsignedInt64(MethodGenerator mv) {
    // operand is always < Long.MAX_VALUE
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2L);
  }
  public static void castUnsignedInt8ToReal32(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2F);
  }
  
  public static void castUnsignedInt8ToReal64(MethodGenerator mv) {
    castUnsignedInt8ToInt(mv);
    mv.visitInsn(Opcodes.I2D);
  }

  /** FROM SIGNED INT 16 (short) **/
  
  public static void castInt16ToBool(MethodGenerator mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castInt16ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt16ToUnsignedInt8(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2B);
  }

  public static void castInt16ToUnsignedInt16(MethodGenerator mv) { 
    // NOOP 
    // Same bitwise representation
  }
  
  public static void castInt16ToInt32(MethodGenerator mv) { 
    // NOOP
    // Signed short and int have same representation on stack
  }
  
  public static void castInt16ToUnsignedInt32(MethodGenerator mv) {
    // NOOP
    // Signed short and int have same representation on stack
  }
  public static void castInt16ToInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2L);
  }
  public static void castInt16ToUnsignedInt64(MethodGenerator mv) {
    // operand will always be < Long.MAX_VALUE
    // so signed/unsigned representations will bitwise equivalent
    mv.visitInsn(Opcodes.I2L);
    
  }
  public static void castInt16ToReal32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  
  public static void castInt16ToReal64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2D);
  }
  
  
  /** UNSIGNED INT (char) **/

  public static void castUnsignedInt16ToBool(MethodGenerator mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castUnsignedInt16ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  public static void castUnsignedInt16ToUnsignedInt8(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castUnsignedInt16ToInt16(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2S); 
  }
  public static void castUnsignedInt16ToInt32(MethodGenerator mv) {
    // NOOP
    // same bitwise representation
  }
  
  public static void castUnsignedInt16ToUnsignedInt32(MethodGenerator mv) { 
    // NOOP
    // Same bitwise representation
  }
  
  public static void castUnsignedInt16ToInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2L);
  }
  
  public static void castUnsignedInt16ToUnsignedInt64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2L);
  }
  
  public static void castUnsignedInt16ToReal32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  public static void castUnsignedInt16ToReal64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2D);
  }

  
  /** SIGNED INT 32 (int) **/
  
  public static void castInt32ToBool(MethodGenerator mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castInt32ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt32ToUnsignedInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt32ToInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2S);
  }

  public static void castInt32ToUnsignedInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2C);
  }

  public static void castInt32ToInt32(MethodGenerator mv) {
    // NOOP
    // Only present because strictly speaking enumerated and integer types are not considered
    // to be exactly the same type
  }

  public static void castInt32ToUnsignedInt32(MethodGenerator mv) { 
    // NOOP 
    // same bitwise representation
  }
  
  public static void castInt32ToInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2L);
  }

  public static void castInt32ToUnsignedInt64(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  
  public static void castInt32ToReal32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2F);
  }
  public static void castInt32ToReal64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2D);
  }
  
  
  
  
  /** FROM UNSIGNED INT 32 */
  
  public static void castUnsignedInt32ToBool(MethodGenerator mv) {
    // NOOP
    // same representation on stack
  }
  
  public static void castUnsignedInt32ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castUnsignedInt32ToUnsignedInt8(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castUnsignedInt32ToInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2S);
  }
  
  public static void castUnsignedInt32ToUnsignedInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.I2C);
  }
  
  public static void castUnsignedInt32ToInt32(MethodGenerator mv) { 
    // NOOP
    // Bitwise representations are the same
  }
  
  public static void castUnsignedInt32ToInt64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.I2L);
    mv.visitLdcInsn(0xffffffffL);
    mv.visitInsn(Opcodes.LAND);
  }
  
  public static void castUnsignedInt32ToUnsignedInt64(MethodGenerator mv) { 
    // operand will always be < Long.MAX value
    // so signed/unsigned representation will be the same
    castUnsignedInt32ToInt64(mv);
  }
  
  public static void castUnsignedInt32ToReal32(MethodGenerator mv) {
    castUnsignedInt32ToInt64(mv);
    mv.visitInsn(Opcodes.L2F);
  }
  
  public static void castUnsignedInt32ToReal64(MethodGenerator mv) {
    castUnsignedInt32ToInt64(mv);
    mv.visitInsn(Opcodes.L2D);
  }

  /* FROM INT 64 (long) */
  
  public static void castInt64ToBool(MethodGenerator mv) {
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castInt64ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.L2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castInt64ToUnsignedInt8(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
 
  public static void castInt64ToInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.L2I);
    mv.visitInsn(Opcodes.I2S);
  }
  
  public static void castInt64ToUnsignedInt16(MethodGenerator mv) { 
    throw new UnsupportedOperationException();
  }
  
  public static void castInt64ToInt32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.L2I);
  }

  public static void castInt64ToUnsignedInt32(MethodGenerator mv) {
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castInt64ToUnsignedInt64(MethodGenerator mv) { 
    // NOOP
    // Bitwise representations are the same
  }
    
  public static void castInt64ToReal32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.L2F);
  }
  
  public static void castInt64ToReal64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.L2D);
  }

  /* FROM UNSIGNED INT 64 (longish) */
   
  public static void castUnsignedInt64ToBool(MethodGenerator mv) {
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castUnsignedInt64ToInt8(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToUnsignedInt8(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToInt16(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToUnsignedInt16(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToInt32(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToUnsignedInt32(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToInt64(MethodGenerator mv) {    
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToReal32(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }
  public static void castUnsignedInt64ToReal64(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }

  /** FROM REAL 32 (float) */
  
  public static void castReal32ToBool(MethodGenerator mv) {
    mv.visitInsn(Opcodes.F2I);
  }
  
  public static void castReal32ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castReal32ToUnsignedInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castReal32ToInt16(MethodGenerator mv) {
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2S);
  }
  
  public static void castReal32ToUnsignedInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2I);
    mv.visitInsn(Opcodes.I2C);
  }
  
  public static void castReal32ToInt32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2I);
  }
  
  public static void castReal32ToUnsignedInt32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2L);
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castReal32ToInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2L);
  }
  
  public static void castReal32ToUnsignedInt64(MethodGenerator mv) {
    mv.visitInsn(Opcodes.F2L);
  }
  
  public static void castReal32ToReal64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.F2D);
  }


  /** FROM REAL 64 (double) */
  
  public static void castReal64ToBool(MethodGenerator mv) {
    mv.visitInsn(Opcodes.D2I);
  }
  
  public static void castReal64ToInt8(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2B);
  }
  
  public static void castReal64ToUnsignedInt8(MethodGenerator mv) {
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2B);
  }

  public static void castReal64ToInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2S);
  }

  public static void castReal64ToUnsignedInt16(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2I);
    mv.visitInsn(Opcodes.I2C);
  }
  
  public static void castReal64ToInt32(MethodGenerator mv) {
    mv.visitInsn(Opcodes.D2L);
    mv.visitInsn(Opcodes.L2I);
  }
  
  
  public static void castReal64ToUnsignedInt32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2L);
    mv.visitInsn(Opcodes.L2I);
  }
  
  public static void castReal64ToInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2L);
  }
  
  public static void castReal64ToUnsignedInt64(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2L);
  }
  
  public static void castReal64ToReal32(MethodGenerator mv) { 
    mv.visitInsn(Opcodes.D2F);
  }

  public static void castReal64ToReal64(MethodGenerator mv) {
    // NOOP
    // May be invoked when casting from long double -> double
  }
}
