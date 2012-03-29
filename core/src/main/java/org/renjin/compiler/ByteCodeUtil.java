package org.renjin.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ByteCodeUtil implements Opcodes{


  public static void pushInt(MethodVisitor mv, int i) {
    if(i <= 5) {
      mv.visitInsn(ICONST_0 + i);
    } else if(i < 127){
      mv.visitIntInsn(BIPUSH, i);
    } else {
      throw new UnsupportedOperationException("more than 127 arguments? something is wrong.");
    }
  }
  
}
