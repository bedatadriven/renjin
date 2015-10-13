package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleType;

public interface PtrGenerator extends ExprGenerator {

  /**
   * 
   * @return the size of this pointer's base type, in bytes
   */
  GimpleType gimpleBaseType();
  
  Type baseType();

  /**
   * 
   * @return true if this pointer is backed by the same array as the {@code other} pointer
   */
  boolean isSameArray(PtrGenerator other);

  /**
   * Pushes the array backing this pointer onto the stack
   */
  void emitPushArray(MethodVisitor mv);

  /**
   * Pushes the integer offset backing this pointer onto the stack
   */
  void emitPushOffset(MethodVisitor mv);
}
