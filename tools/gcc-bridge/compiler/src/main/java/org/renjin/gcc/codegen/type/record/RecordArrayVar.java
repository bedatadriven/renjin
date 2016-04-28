package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;
import static org.renjin.gcc.codegen.expr.Expressions.zero;

/**
 * An expression representing a local variable or parameter with the value of a record, using the 
 * {@link RecordArrayTypeStrategy}.
 */
public class RecordArrayVar implements RecordArrayExpr {
  
  private SimpleExpr array;
  private int arrayLength;

  public RecordArrayVar(SimpleExpr array, int arrayLength) {
    this.array = array;
    this.arrayLength = arrayLength;
  }

  @Override
  public Expr addressOf() {
    return new FatPtrExpr(array, zero());
  }

  @Override
  public void store(MethodGenerator mv, Expr rhs) {
    RecordArrayExpr arrayRhs = (RecordArrayExpr) rhs;
    mv.arrayCopy(arrayRhs.getArray(), arrayRhs.getOffset(), array, zero(), constantInt(arrayLength));
  }

  @Override
  public SimpleExpr getArray() {
    return array;
  }
  
  @Override
  public SimpleExpr getOffset() {
    return zero();
  }

  @Override
  public SimpleExpr arrayForReturning() {
    // We can get away with returning the array without copying, because IF this local variable has escaped,
    // that pointer would be invalid anyway, because the record value should go out of scope when the function
    // returns.

    // For example:
    // point add_point(point a, point b) {
    //     point c;
    //     c.x = a.x + b.x;
    //     c.y = a.y + b.y;
    //     global_pointer = &c;
    //     return c;
    // }   
    
    // Technically, there is another reference out there to c, and the return value SHOULD be different
    // than the value pointed to by global_pointer, but any C program that attempted to change the value 
    // pointed to by global_pointer after add_point returns, would fail. So it shouldn't matter to us, and we 
    // can avoid copying the array, which the JVM also allocates on the heap and not the stack as would be the case
    // with C.
    
    return array;
  }
}
