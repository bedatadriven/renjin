package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;

/**
 * Creates generators for void value types. Only used for return types.
 */
public class VoidTypeStrategy implements TypeStrategy<GExpr> {

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidReturnStrategy();
  }
  
  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("parameters cannot have 'void' type");
  }

  @Override
  public GExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    throw new UnsupportedOperationException("variables cannot have 'void' type");
  }

  @Override
  public GExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("constructors cannot have 'void' type");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("fields cannot have 'void' type");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("fields cannot have 'void' type");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VoidPtrStrategy();
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("arrays cannot have component type of 'void'");
  }

  @Override
  public GExpr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

}
