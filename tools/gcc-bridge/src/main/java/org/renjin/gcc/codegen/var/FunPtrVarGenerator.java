package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Emits store/load instructions for a Function Pointer Variable.
 * 
 * <p>We compile this to bytecode as a local variable holding a method handle.</p>
 */
public class FunPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleFunctionType type;
  private int index;

  public FunPtrVarGenerator(GimpleFunctionType type, int index) {
    this.type = type;
    this.index = index;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(type);
  }

  @Override
  public void emitPushMethodHandle(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, index);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushMethodHandle(mv);
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }
}
