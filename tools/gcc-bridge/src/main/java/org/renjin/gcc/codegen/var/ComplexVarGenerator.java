package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;

/**
 * Generates load/store for a variable of type {@code complex}
 * 
 * <p>We compile this as two double local variables.</p>
 */
public class ComplexVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleComplexType type;
  private Type partType;
  private Var realVar;
  private Var imaginaryVar;

  public ComplexVarGenerator(GimpleComplexType type, Var realVar, Var imaginaryVar) {
    this.type = type;
    this.partType = type.getJvmPartType();
    this.realVar = realVar;
    this.imaginaryVar = imaginaryVar;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public GimpleComplexType getGimpleType() {
    return type;
  }


  @Override
  public ExprGenerator realPart() {
    return new PrimitiveVarGenerator(type.getPartType(), realVar);
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new PrimitiveVarGenerator(type.getPartType(), imaginaryVar);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    // store real part
    valueGenerator.realPart().emitPrimitiveValue(mv);
    realVar.store(mv);
    
    // store imaginary part
    valueGenerator.imaginaryPart().emitPrimitiveValue(mv);
    imaginaryVar.store(mv);
  }

}
