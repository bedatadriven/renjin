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
  
  void emitPushArrayAndOffset(MethodVisitor mv);
}
