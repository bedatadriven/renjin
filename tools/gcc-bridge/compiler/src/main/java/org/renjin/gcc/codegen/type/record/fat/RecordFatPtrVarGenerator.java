package org.renjin.gcc.codegen.type.record.fat;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimpleType;

public class RecordFatPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private final RecordClassTypeStrategy recordTypeStrategy;
  private final Var array;
  private final Var offset;

  public RecordFatPtrVarGenerator(RecordClassTypeStrategy recordTypeStrategy, Var array, Var offset) {
    this.recordTypeStrategy = recordTypeStrategy;
    this.array = array;
    this.offset = offset;
  }

  @Override
  public GimpleType getGimpleType() {
    return recordTypeStrategy.getRecordType().pointerTo();
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    array.load(mv);
    offset.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPtrArrayAndOffset(mv);
    offset.store(mv);
    array.store(mv);
  }

  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new RecordFatPtrPlus(recordTypeStrategy, this, offsetInBytes); 
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedRecordFatPtr(recordTypeStrategy, this);
  }
}
