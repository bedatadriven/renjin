package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;

public interface ReturnGenerator {
  
  Type getType();

  void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator);
  
  void emitVoidReturn(MethodVisitor mv);
  
}
