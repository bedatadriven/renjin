package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.type.record.fat.DereferencedRecordPtr;
import org.renjin.gcc.codegen.type.record.fat.RecordPtrPtrPlus;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Pointer to pointers of Records.
 * 
 * <p>Compile using an array of ObjectPtr</p>
 */
public class RecordPtrPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private RecordTypeStrategy strategy;
  private final Var arrayVar;
  private final Var offsetVar;

  public RecordPtrPtrVarGenerator(RecordTypeStrategy strategy, Var arrayVar, Var offsetVar) {
    this.strategy = strategy;
    this.arrayVar = arrayVar;
    this.offsetVar = offsetVar;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    arrayVar.load(mv);
    offsetVar.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPtrArrayAndOffset(mv);
    offsetVar.store(mv);
    arrayVar.store(mv);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(new GimplePointerType(strategy.getRecordType()));
  }

  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new RecordPtrPtrPlus(strategy, this, offsetInBytes);
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedRecordPtr(strategy, this);
  }
}
