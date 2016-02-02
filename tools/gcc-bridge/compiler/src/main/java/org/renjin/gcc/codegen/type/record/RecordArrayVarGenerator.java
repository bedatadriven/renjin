package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.codegen.type.record.fat.AddressOfRecordArray;
import org.renjin.gcc.codegen.type.record.fat.RecordArrayElement;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

public class RecordArrayVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private final Var arrayVar;
  private GimpleArrayType arrayType;
  private RecordClassTypeStrategy strategy;

  public RecordArrayVarGenerator(GimpleArrayType arrayType, RecordClassTypeStrategy strategy, Var arrayVar) {
    this.arrayType = arrayType;
    this.strategy = strategy;
    this.arrayVar = arrayVar;
    Preconditions.checkArgument(arrayType.getLbound() == 0);
  }
  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue) {
    PrimitiveConstGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, strategy.getJvmType().getInternalName());
    for(int i=0;i<arrayType.getElementCount();++i) {
      // keep the array on the stack
      mv.visitInsn(Opcodes.DUP);

      // index of the element to store
      PrimitiveConstGenerator.emitInt(mv, i);
      
      // create a new instance of the record class
      strategy.emitConstructor(mv);
      
      // store the new instance to the array
      mv.visitInsn(Opcodes.AASTORE);
    }
    
    // Store the array to the local variable
    arrayVar.store(mv, );
    
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushArray(MethodGenerator mv) {
    arrayVar.load(mv);
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushArray(mv);
    arrayVar.store(mv, );
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new RecordArrayElement(strategy, this, indexGenerator);  
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfRecordArray(this);
  }
}
