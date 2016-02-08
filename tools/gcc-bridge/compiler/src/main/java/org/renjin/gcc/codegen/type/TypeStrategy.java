package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 *  Provides strategies for code generation for a specific {@code GimpleType}
 *  
 */
public abstract class TypeStrategy<ExprT extends ExprGenerator> {

  /**
   * 
   * @return the {@link ParamStrategy} for this type.
   */
  public ParamStrategy getParamStrategy() {
    throw new UnimplementedException(getClass(), "getParamStrategy");
  }

  /**
   * @return the {@link ReturnStrategy} for this type.
   */
  public ReturnStrategy getReturnStrategy() {
    throw new UnimplementedException(getClass(), "getReturnStrategy");
  }


  /**
   * Creates a {@link VarGenerator} for a {@link GimpleVarDecl} of this type
   */
  public ExprT varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
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

  public ExprGenerator elementAt(ExprGenerator array, ExprGenerator index) {
    throw new UnimplementedException(getClass(), "elementAt");
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

  /**
   * Creates a new ExprGenerator that allocates a new pointer
   * @param mv
   * @param length the size of the memory to allocate, <strong>in number of elements, not bytes!</strong>
   */
  public ExprT malloc(MethodGenerator mv, Value length) {
    throw new UnsupportedOperationException("TODO: implement malloc() in " + getClass().getName());
  }

  public ExprT realloc(ExprT pointer, Value length) {
    throw new UnsupportedOperationException("TODO: implement realloc() in " + getClass().getName());
  }

  public ExprT pointerPlus(ExprT pointer, Value offsetInBytes) {
    throw new UnimplementedException(getClass(), "pointerPlus");
  }

  public ExprGenerator valueOf(ExprT pointerExpr) {
    throw new UnsupportedOperationException("TODO: implement valueOf() in " + getClass().getName());
  }
  
  public ExprT nullPointer() {
    throw new UnsupportedOperationException("TODO: implement nullPointer() in " + getClass().getName());
  }
  
  public ExprT constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO: implement constructorExpr() in " + getClass().getName());
  }

  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    throw new UnimplementedException(getClass(), "addressableFieldGenerator");
  }

  public ConditionGenerator comparePointers(GimpleOp op, ExprT x, ExprT y) {
    throw new UnimplementedException(getClass(), "comparePointers");
  }

  public ExprGenerator addressOf(ExprT value) {
    throw new UnimplementedException(getClass(), "addressableFieldGenerator");
  }

  public Value memoryCompare(ExprT p1, ExprT p2, Value n) {
    throw new UnimplementedException(getClass(), "memoryCompare");
  }

  public void memoryCopy(MethodGenerator mv, ExprT destination, ExprT source, Value length) {
    throw new UnimplementedException(getClass(), "memoryCopy");
  }
}
