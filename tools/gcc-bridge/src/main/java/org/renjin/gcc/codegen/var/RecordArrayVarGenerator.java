package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.arrays.RecordArrayElement;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
import org.renjin.gcc.codegen.pointers.AddressOfRecordArray;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

public class RecordArrayVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private final Var varIndex;
  private GimpleArrayType arrayType;
  private RecordClassGenerator generator;

  public RecordArrayVarGenerator(GimpleArrayType arrayType, RecordClassGenerator generator, Var varIndex) {
    this.arrayType = arrayType;
    this.generator = generator;
    this.varIndex = varIndex;
    Preconditions.checkArgument(arrayType.getLbound() == 0);
  }
  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    PrimitiveConstValueGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, generator.getType().getInternalName());
    for(int i=0;i<arrayType.getElementCount();++i) {
      // keep the array on the stack
      mv.visitInsn(Opcodes.DUP);

      // index of the element to store
      PrimitiveConstValueGenerator.emitInt(mv, i);
      
      // create a new instance of the record class
      generator.emitConstructor(mv);
      
      // store the new instance to the array
      mv.visitInsn(Opcodes.AASTORE);
    }
    
    // Store the array to the local variable
    varIndex.store(mv);
    
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    varIndex.load(mv);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new RecordArrayElement(generator, this, indexGenerator);  
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfRecordArray(this);
  }
}
