package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Root interface for generators which emit bytecode instructions
 */
public interface ExprGenerator {

  /**
   * 
   * @return the {@code GimpleType} of the underlying expression
   */
  GimpleType getGimpleType();

  /**
   * 
   * @return an ExprGenerator for the value to which this {@code ExpressionGenerator} points
   */
  ExprGenerator valueOf();

  /**
   * @return an ExprGenerator for this value's address.
   */
  ExprGenerator addressOf();

  /**
   * 
   * @return an ExprGenerator for this complex value's real part
   */
  ExprGenerator realPart();

  /**
   * 
   * @return an ExprGenerator for this complex value's imaginary part
   */
  ExprGenerator imaginaryPart();

  /**
   * 
   * @param indexGenerator a generator for the array element's index
   * @return a generator which can load/store values to this array
   */
  ExprGenerator elementAt(ExprGenerator indexGenerator);

  /**
   * 
   * @return the JVM type of the value pushed 
   * @throws UnsupportedOperationException if this is not a value expression
   */
  Type getValueType();

  /**
   * 
   * @return true if this is a {@code ConstantGenerator} for an int32 constant equal to {@code value}
   */
  boolean isConstantIntEqualTo(int value);


  /**
   * 
   * @return the {@code WrapperType} used to implement this pointer type
   * @throws UnsupportedOperationException if this is not a pointer expression
   */
  WrapperType getPointerType();

  /**
   * Writes the code to push this value on the stack.
   * @throws UnsupportedOperationException if this is not a value expression
   */
  void emitPushValue(MethodVisitor mv);

  /**
   * Writes the code push the array and offset backing this pointer onto the stack
   * @throws UnsupportedOperationException if this is not a pointer expression
   */
  void emitPushPtrArrayAndOffset(MethodVisitor mv);

  /**
   * Writes the code to push the reference onto the stack
   * @throws UnsupportedOperationException if this is not a pointer with a reference representation
   */
  void emitPushMethodHandle(MethodVisitor mv);

  /**
   * Writes the code to push a pointer wrapper instance onto the stack 
   * @throws UnsupportedOperationException if this is not a pointer expression
   */ 
  void emitPushPointerWrapper(MethodVisitor mv);


  /**
   * Writes the code to push this complex value onto the stack as a {@code double[]} or {@code float[]} of length 2
   * @throws UnsupportedOperationException if this is not a complex number expression
   */
  void emitPushComplexAsArray(MethodVisitor mv);
  
  /**
   * 
   * Emits a store instruction; to a variable, to an array value, field, etc
   * 
   * @param valueGenerator the generator which produces the value to be stored
   */
  void emitStore(MethodVisitor mv, ExprGenerator valueGenerator);

}
