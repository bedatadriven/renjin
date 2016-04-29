package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Addressable;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Record value expression, backed by a JVM primitive array 
 */
public final class RecordArrayExpr implements LValue, Addressable {


  private SimpleExpr array;
  private SimpleExpr offset;
  private int arrayLength;

  public RecordArrayExpr(SimpleExpr array, SimpleExpr offset,  int arrayLength) {
    this.array = array;
    this.offset = offset;
    this.arrayLength = arrayLength;
  }

  public RecordArrayExpr(SimpleExpr array, int arrayLength) {
    this(array, zero(), arrayLength);
  }

  @Override
  public Expr addressOf() {
    return new FatPtrExpr(array, offset);
  }

  @Override
  public void store(MethodGenerator mv, Expr rhs) {
    RecordArrayExpr arrayRhs = (RecordArrayExpr) rhs;
    mv.arrayCopy(arrayRhs.getArray(), arrayRhs.getOffset(), array, offset, constantInt(arrayLength));
  }

  public SimpleExpr getArray() {
    return array;
  }

  public SimpleExpr getOffset() {
    return offset;
  }

  public SimpleExpr copyArray() {
    return copyOfArrayRange(array, offset, sum(offset, arrayLength));
  }
}
