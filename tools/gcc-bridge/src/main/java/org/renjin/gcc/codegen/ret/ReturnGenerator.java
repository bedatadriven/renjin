package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

public interface ReturnGenerator {
  
  Type getType();
  
  GimpleType getGimpleType();
  
  void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator);
  
  void emitVoidReturn(MethodVisitor mv);

}
