package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 *  Provides strategies for code generation for a specific {@code GimpleType}
 *  
 */
public abstract class TypeStrategy {

  /**
   * 
   * @return the {@link ParamStrategy} for this type.
   */
  public ParamStrategy getParamStrategy() {
    throw new UnimplementedException(getClass(), "getParamStrategy");
  }

  /**
   * 
   * @return the {@link ReturnStrategy} for this type.
   */
  public ReturnStrategy getReturnStrategy() {
    throw new UnimplementedException(getClass(), "getReturnStrategy");
  }


  /**
   * Creates a {@link VarGenerator} for a {@link GimpleVarDecl} of this type
   */
  public ExprGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    throw new UnimplementedException(getClass(), "varGenerator");
  }

  /**
   * @return a {@code TypeStrategy} for the pointer type to this type.
   */
  public TypeStrategy pointerTo() {
    throw new UnimplementedException(getClass(), "pointerTo");
  }

  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw new UnimplementedException(getClass(), "arrayOf");
  }

  /**
   * Creates a new FieldGenerator for fields of this type.
   * 
   * @param className the full internal name of the class, for example, "org/renjin/gcc/Record$1"
   * @param fieldName the name of the field
   */
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    throw new UnsupportedOperationException("TODO: implement fieldGenerator() in " + getClass().getName());
  }

  public ExprGenerator mallocExpression(ExprGenerator size) {
    throw new UnsupportedOperationException("TODO: implement mallocExpression() in " + getClass().getName());
  }

  public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO: implement constructorExpr() in " + getClass().getName());
  }

  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    throw new UnimplementedException(getClass(), "addressableFieldGenerator");
  }
  
}
