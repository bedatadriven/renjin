package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.ValueBounds;

import java.util.Map;


/**
 * Extracts a single element from a vector. 
 */
public class ElementAccess extends SpecializedCallExpression {

  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;
  
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
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {

//    int stackIncrease =
//        getVector().emitPush(emitContext, mv) +
//        getIndex().emitPush(emitContext, mv);
//
//    if(type.equals(double.class)) {
//      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
//          Type.getInternalName(Vector.class), "getElementAsDouble", "(I)D", true);
//    } else {
//      throw new UnsupportedOperationException(type.toString());
//    }
//
//    return stackIncrease;
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    int typeSet = getVector().updateTypeBounds(typeMap).getTypeSet();
    
    return ValueBounds.primitive(typeSet);
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }
}