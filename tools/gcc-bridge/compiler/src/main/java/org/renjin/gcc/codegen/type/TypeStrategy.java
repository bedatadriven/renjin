package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 *  Provides strategies for code generation for a specific {@code GimpleType}
 *  
 */
public interface TypeStrategy<ExprT extends Expr> {

  /**
   * 
   * @return the {@link ParamStrategy} for this type.
   */
  ParamStrategy getParamStrategy();

  /**
   * @return the {@link ReturnStrategy} for this type.
   */
  ReturnStrategy getReturnStrategy();

  /**
   * Creates an expression generator for {@link GimpleVarDecl}s of this type
   */
  ExprT variable(GimpleVarDecl decl, VarAllocator allocator);

  /**
   * Creates an expression generator for constructors of this type.
   */
  ExprT constructorExpr(ExprFactory exprFactory, GimpleConstructor value);

  /**
   * Creates a new FieldGenerator for fields of this type.
   *
   * @param className the full internal name of the class, for example, "org/renjin/gcc/Record$1"
   * @param fieldName the name of the field
   */
  FieldStrategy fieldGenerator(String className, String fieldName);


  FieldStrategy addressableFieldGenerator(String className, String fieldName);
  
  /**
   * @return a {@code PointerTypeStrategy} for pointers of this type
   */
  PointerTypeStrategy pointerTo();

  /**
   * @param arrayType 
   * @return a strategy for arrays of this type
   */
  ArrayTypeStrategy arrayOf(GimpleArrayType arrayType);

}
