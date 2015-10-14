package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.PointerTypes;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

/**
 * Generates the code to wrap and return a pointer
 */
public class PtrReturnGenerator implements ReturnGenerator {
  
  private GimpleIndirectType type;
  private Type wrapperType;

  public PtrReturnGenerator(GimpleType type) {
    this.type = (GimpleIndirectType) type;
    this.wrapperType = PointerTypes.wrapperType(this.type.getBaseType());
  }

  @Override
  public Type type() {
    return wrapperType;
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    PtrGenerator ptrGenerator = (PtrGenerator) valueGenerator;

    
    // Create a new instance of the wrapper
    mv.visitTypeInsn(Opcodes.NEW, wrapperType.getInternalName());
    
    // Initialize it with the array and offset
    mv.visitInsn(Opcodes.DUP);
    ptrGenerator.emitPushArray(mv);
    ptrGenerator.emitPushOffset(mv);
    mv.visitMethodInsn(INVOKESPECIAL, wrapperType.getInternalName(), "<init>", 
        PointerTypes.getConstructorDescriptor(type.getBaseType()), false);
  
    // return
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
}
