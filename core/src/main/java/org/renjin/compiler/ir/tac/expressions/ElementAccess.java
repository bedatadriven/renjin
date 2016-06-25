package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.TypeBounds;
import org.renjin.sexp.Vector;

import java.util.Map;


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
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {

    int stackIncrease =
        getVector().emitPush(emitContext, mv) +
        getIndex().emitPush(emitContext, mv);

    if(type.equals(double.class)) {
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
          Type.getInternalName(Vector.class), "getElementAsDouble", "(I)D", true);
    } else {
      throw new UnsupportedOperationException(type.toString());
    }

    return stackIncrease;
  }

  @Override
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> typeMap) {
    TypeBounds vectorType = getVector().computeTypeBounds(typeMap);
    return TypeBounds.primitive(vectorType.getTypeMask());
  }
}