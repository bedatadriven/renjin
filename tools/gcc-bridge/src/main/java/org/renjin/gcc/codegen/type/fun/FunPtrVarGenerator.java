package org.renjin.gcc.codegen.type.fun;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates store/load instructions for a Function Pointer Variable.
 * 
 * <p>We compile this to bytecode as a local variable holding a method handle.</p>
 */
public class FunPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleFunctionType type;
  private Var var;

  public FunPtrVarGenerator(GimpleFunctionType type, Var var) {
    this.type = type;
    this.var = var;
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
    var.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushMethodHandle(mv);
    var.store(mv);
  }
}
