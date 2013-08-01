package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;


/**
 * Extracts a single element from a vector.
 */
public class ElementAccess extends SpecializedCallExpression {

  
  public ElementAccess(Expression vector, Expression index) {
    super(vector, index);
  }

  public Expression getVector() {
    return arguments[0];
  }

  /**
   * @return the value holding the zero-based index of the
   * element to extract
   */
  public Expression getIndex() {
    return arguments[1];
  }

  @Override
  public String toString() {
    return getVector() + "[" + getIndex() + "]";
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class inferType() {
    Class vectorType = getVector().inferType();
    if(DoubleVector.class.isAssignableFrom(vectorType)) {
      return double.class;
    } else if(IntVector.class.isAssignableFrom(vectorType)) {
      return int.class;
    } else {
      throw new UnsupportedOperationException("can't figure out type of " + getVector());
    }
  }

}