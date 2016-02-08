package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleFunctionType;

/**
 * Creates {@code Generators} for values for function values.
 * 
 * <p>Function pointers are compiled to {@link java.lang.invoke.MethodHandle}s, but since Gimple
 * is statically typed, we don't need the {@code invokedynamic} bytecode and can simply use
 * {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} to invoke function calls.</p>
 */ 
public class FunTypeStrategy implements TypeStrategy<SimpleExpr> {

  private GimpleFunctionType type;

  public FunTypeStrategy(GimpleFunctionType type) {
    this.type = type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw newInvalidOperation();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw newInvalidOperation();
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    throw newInvalidOperation();
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw newInvalidOperation();
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    throw newInvalidOperation();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    throw newInvalidOperation();
  }
  
  @Override
  public PointerTypeStrategy pointerTo() {
    return new FunPtrStrategy();
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw newInvalidOperation();
  }

  private UnsupportedOperationException newInvalidOperation() {
    return new UnsupportedOperationException("Invalid operation for function value type. " +
        "(Should this be a function *pointer* instead?");
  }


}
