package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.analysis.AddressableFinder;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

public class PrimitiveVarGenerator extends AbstractExprGenerator implements VarGenerator {
  private GimplePrimitiveType type;
  private Var var;

  public PrimitiveVarGenerator(GimpleType type, Var var) {
    this.var = var;
    this.type = (GimplePrimitiveType) type;
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPrimitiveValue(mv);

    Preconditions.checkArgument(checkTypes(valueGenerator),
        "Type mismatch: Cannot assign %s of type %s to %s of type %s",
        valueGenerator,
        valueGenerator.getJvmPrimitiveType(),
        this,
        getJvmPrimitiveType());
    
    var.store(mv);
  }

  private boolean checkTypes(ExprGenerator valueGenerator) {
    Type varType = getJvmPrimitiveType();
    Type valueType = valueGenerator.getJvmPrimitiveType();
 
    return (isIntType(varType) && isIntType(valueType)) ||
           varType.equals(valueType);
  }

  private boolean isIntType(Type type) {
    return type.equals(Type.BOOLEAN_TYPE) ||
           type.equals(Type.BYTE_TYPE) ||
           type.equals(Type.INT_TYPE);
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    var.load(mv);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public ExprGenerator addressOf() {
    throw new InternalCompilerException("Variable is not addressable. Apparently, " +
        AddressableFinder.class.getName() + " failed to mark this variable as addressable " +
        "during the analysis pass.");
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

}
