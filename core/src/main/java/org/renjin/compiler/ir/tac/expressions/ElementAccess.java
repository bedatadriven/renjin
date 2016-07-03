package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

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
  public int load(EmitContext emitContext, InstructionAdapter mv) {

    int stackHeight;
    
    Expression vector = getVector();
    Type resultType = valueBounds.storageType();

    ValueBounds vectorBounds = vector.getValueBounds();
    if(vectorBounds.isConstant()) {
      if(vectorBounds.getConstantValue() instanceof IntSequence) {
        IntSequence sequence = (IntSequence) vectorBounds.getConstantValue();
        getIndex().load(emitContext, mv);
        if (sequence.getBy() != 1) {
          mv.iconst(sequence.getBy());
          mv.mul(Type.INT_TYPE);
        }
        if (sequence.getFrom() != 0) {
          mv.iconst(sequence.getFrom());
          mv.add(Type.INT_TYPE);
        }
        return 2;
      }
    }
    
    if(vector.getType().getSort() == Type.OBJECT) {
      stackHeight = vector.load(emitContext, mv);
      
      if(vector.getType().equals(Type.getType(SEXP.class))) {
        mv.checkcast(Type.getType(Vector.class));
      }
      
      getIndex().load(emitContext, mv);
      
      if(resultType.equals(Type.INT_TYPE)) {
        mv.invokeinterface(Type.getInternalName(Vector.class), "getElementAsInt",
            Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE));
      } else {
        throw new UnsupportedOperationException("resultType: " + resultType);
      }
      
      return stackHeight+1;
      
    } else {
      throw new UnsupportedOperationException("vectorType: " + vector.getType());
    }
  }

  @Override
  public Type getType() {
    return valueBounds.storageType();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    int typeSet = getVector().updateTypeBounds(typeMap).getTypeSet();
      
    valueBounds = ValueBounds.primitive(typeSet);
    
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }
}