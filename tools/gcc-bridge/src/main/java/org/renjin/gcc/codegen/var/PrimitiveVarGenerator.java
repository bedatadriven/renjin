package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.analysis.AddressableFinder;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

public class PrimitiveVarGenerator extends AbstractExprGenerator implements VarGenerator {
  private GimplePrimitiveType type;
  private int localVarIndex;

  public PrimitiveVarGenerator(GimpleType type, int localVarIndex) {
    this.localVarIndex = localVarIndex;
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
    
    mv.visitVarInsn(getJvmPrimitiveType().getOpcode(ISTORE), localVarIndex);

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
    mv.visitVarInsn(getJvmPrimitiveType().getOpcode(ILOAD), localVarIndex);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitDebugging(MethodVisitor mv, String name, Label start, Label end) {
    mv.visitLocalVariable(name, type.jvmType().getDescriptor(), null, start, end, localVarIndex);
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
