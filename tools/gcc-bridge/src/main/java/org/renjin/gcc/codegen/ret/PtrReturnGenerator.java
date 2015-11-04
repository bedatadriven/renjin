package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the code to wrap and return a pointer
 */
public class PtrReturnGenerator implements ReturnGenerator {
  
  private GimpleIndirectType type;
  private WrapperType wrapperType;

  public PtrReturnGenerator(GimpleType type) {
    this.type = (GimpleIndirectType) type;
    this.wrapperType = WrapperType.of(this.type.getBaseType());
  }

  @Override
  public Type getType() {
    return wrapperType.getWrapperType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    PtrGenerator ptrGenerator = (PtrGenerator) valueGenerator;
    
    wrapperType.emitPushNewWrapper(mv, ptrGenerator);

    // return
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
}
