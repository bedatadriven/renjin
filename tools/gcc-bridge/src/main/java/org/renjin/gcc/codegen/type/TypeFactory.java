package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 * Responsible for creating Generators for parameters, local variables, and return values
 * for a specific Gimple type.
 */
public abstract class TypeFactory {

  /**
   * 
   * @return a new {@code ParamGenerator} for this type.
   */
  public ParamGenerator paramGenerator() {
    throw new UnimplementedException(getClass(), "paramGenerator");
  }

  /**
   * 
   * @return a new {@code ReturnGenerator} for this type.
   */
  public ReturnGenerator returnGenerator() {
    throw new UnimplementedException(getClass(), "returnGenerator");
  }


  public VarGenerator varGenerator(LocalVarAllocator allocator) {
    throw new UnimplementedException(getClass(), "varGenerator");
  }

  public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
    throw new UnimplementedException(getClass(), "addressableVarGenerator");
  }


  public TypeFactory pointerTo() {
    throw new UnimplementedException(getClass(), "pointerTo");
  }

  public TypeFactory arrayOf(GimpleArrayType arrayType) {
    throw new UnimplementedException(getClass(), "arrayOf");
  }

  /**
   * Creates a new FieldGenerator for fields of this type.
   * 
   * @param className the full internal name of the class, for example, "org/renjin/gcc/Record$1"
   * @param fieldName the name of the field
   */
  public FieldGenerator fieldGenerator(String className, String fieldName) {
    throw new UnsupportedOperationException("TODO: implement fieldGenerator() in " + getClass().getName());
  }

  public ExprGenerator mallocExpression(ExprGenerator size) {
    throw new UnsupportedOperationException("TODO: implement mallocExpression() in " + getClass().getName());
  }

  public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO: implement constructorExpr() in " + getClass().getName());
  }
}
