package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Preconditions;
import com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;


/**
 * Extracts a single element from a vector.
 */
public class ElementAccess extends SpecializedCallExpression {

  private Class type;
  
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

    getVector().emitPush(emitContext, mv);

    if(type.equals(double.class)) {
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
        "org/renjin/sexp/Vector", "getElementAsDouble", "(I)D");
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public Class getType() {
    Preconditions.checkNotNull(type, "type not resolved");
    return type;
  }

  @Override
  public boolean isTypeResolved() {
    return type != null;
  }

  @Override
  public void resolveType() {
    Class vectorClass = getVector().getType();
    if(IntVector.class.isAssignableFrom(vectorClass)) {
      this.type = int.class;
    } else if(AtomicVector.class.isAssignableFrom(vectorClass)) {
      this.type = double.class;
    } else {
      throw new UnsupportedOperationException(getVector() + ":" + vectorClass.getName());
    }
  }
}