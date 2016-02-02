package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

public class RecordVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private Var var;
  private RecordClassTypeStrategy strategy;

  public RecordVarGenerator(RecordClassTypeStrategy strategy, Var var) {
    this.strategy = strategy;
    this.var = var;
  }

  @Override
  public void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue) {
    mv.visitTypeInsn(NEW, strategy.getJvmType().getInternalName());
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, strategy.getJvmType().getInternalName(), "<init>", "()V", false);
    var.store(mv, );
    
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public ExprGenerator addressOf() {
    return new Pointer();
  }

  @Override
  public GimpleType getGimpleType() {
    return strategy.getRecordType();
  }

  public ExprGenerator memberOf(String name) {
    return strategy.getFieldGenerator(name).memberExprGenerator(addressOf());
  }
  
  public class Pointer extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(strategy.getRecordType());
    }

    @Override
    public void emitPushRecordRef(MethodGenerator mv) {
      var.load(mv);
    }
  }
}
