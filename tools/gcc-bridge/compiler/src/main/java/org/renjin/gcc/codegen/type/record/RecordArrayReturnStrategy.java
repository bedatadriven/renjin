package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;

/**
 * Strategy for returning record values represented by arrays.
 * 
 * <p>In C, returning a {@code struct} value, as opposed to </p>
 * 
 */
public class RecordArrayReturnStrategy implements ReturnStrategy {

  private Type arrayType;
  private int arrayLength;

  public RecordArrayReturnStrategy(Type arrayType, int arrayLength) {
    Preconditions.checkArgument(arrayType.getSort() == Type.ARRAY, "Not an array type: " + arrayType);
    this.arrayType = arrayType;
    this.arrayLength = arrayLength;
  }

  @Override
  public Type getType() {
    return arrayType;
  }

  public Type getArrayComponentType() {
    // array type is [B for example,
    // so strip [
    String descriptor = arrayType.getDescriptor();
    return Type.getType(descriptor.substring(1));
  }

  /**
   * Returns an expression representing a copy of the given array value.
   *
   */
  @Override
  public SimpleExpr marshall(Expr value) {
    RecordArrayExpr arrayValue = (RecordArrayExpr) value;
    return arrayValue.copyArray();
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return new RecordArrayExpr(returnValue, arrayLength);
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    return Expressions.newArray(getArrayComponentType(), arrayLength);
  }
}
