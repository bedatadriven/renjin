package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;

/**
 *  Provides strategies for code generation for a specific {@code GimpleType}
 *  
 */
public interface TypeStrategy<ExprT extends GExpr> {

  /**
   * 
   * @return the {@link ParamStrategy} for this type.
   */
  ParamStrategy getParamStrategy();

  /**
   * @return the {@link ReturnStrategy} for this type.
   */
  ReturnStrategy getReturnStrategy();
  
  ValueFunction getValueFunction();

  /**
   * Creates an expression generator for {@link GimpleVarDecl}s of this type
   */
  ExprT variable(GimpleVarDecl decl, VarAllocator allocator);

  /**
   * Creates an expression generator for constructors of this type.
   */
  ExprT constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value);

  /**
   * Creates a new FieldGenerator for fields of this type.
   *
   * @param className the full internal name of the class, for example, "org/renjin/gcc/Record$1"
   * @param fieldName the name of the field
   */
  FieldStrategy fieldGenerator(Type className, String fieldName);


  FieldStrategy addressableFieldGenerator(Type className, String fieldName);
  
  /**
   * @return a {@code PointerTypeStrategy} for pointers of this type
   */
  PointerTypeStrategy pointerTo();

  /**
   * @param arrayType 
   * @return a strategy for arrays of this type
   */
  ArrayTypeStrategy arrayOf(GimpleArrayType arrayType);

  /**
   * 
   * Casts the given {@code value}, compield with the given {@code typeStrategy}, 
   * to a value of this strategy.
   */
  ExprT cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException;


}
