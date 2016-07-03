package org.renjin.compiler.builtins;

import com.google.common.collect.Lists;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.annotations.PreserveAttributeStyle;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AttributeMap;

import java.util.Iterator;
import java.util.List;

/**
 * Specialization for builtins that are marked {@link org.renjin.invoke.annotations.DataParallel} and
 * whose arguments are "recycled" for multiple calls.
 */
public class DataParallelCall implements Specialization {

  private final String name;
  private final JvmMethod method;
  private List<ValueBounds> argumentTypes;
  private final ValueBounds valueBounds;
  private final Type type;

  public DataParallelCall(Primitives.Entry primitive, JvmMethod method, List<ValueBounds> argumentTypes) {
    this.name = primitive.name;
    this.method = method;
    this.argumentTypes = argumentTypes;
    this.valueBounds = computeBounds(argumentTypes);
    this.type = valueBounds.storageType();
  }

  
  private ValueBounds computeBounds(List<ValueBounds> argumentBounds) {
    
    List<ValueBounds> recycledArguments = recycledArgumentBounds(argumentBounds);
    
    ValueBounds bounds = ValueBounds.vector(method.getReturnType(), computeResultLength(argumentTypes));
    
    if(method.getPreserveAttributesStyle() == PreserveAttributeStyle.NONE) {
      bounds = bounds.withAttributes(AttributeMap.EMPTY);      
    
    } else if(bounds.isLengthConstant()) {
      bounds = bounds.withAttributes(computeResultAttributes(recycledArguments, bounds.getLength()));
    }
    
    return bounds;
  }


  /**
   * Makes a list of {@link ValueBounds} for @Recycled arguments.
   */
  private List<ValueBounds> recycledArgumentBounds(List<ValueBounds> argumentBounds) {
    List<ValueBounds> list = Lists.newArrayList();
    Iterator<ValueBounds> argumentIt = argumentBounds.iterator();
    for (JvmMethod.Argument formal : method.getFormals()) {
      if (formal.isRecycle()) {
        list.add(argumentIt.next());
      }
    }
    return list;
  }
  
  private int computeResultLength(List<ValueBounds> argumentBounds) {
    Iterator<ValueBounds> it = argumentBounds.iterator();
    int resultLength = 0;
    
    while(it.hasNext()) {
      int argumentLength = it.next().getLength();
      if(argumentLength == ValueBounds.UNKNOWN_LENGTH) {
        return ValueBounds.UNKNOWN_LENGTH;
      }
      if(argumentLength == 0) {
        return 0;
      }
      resultLength = Math.max(resultLength, argumentLength);
    }

    return resultLength;
  }
  
  private AttributeMap computeResultAttributes(List<ValueBounds> argumentBounds, int resultLength) {

    AttributeMap.Builder attributes = AttributeMap.newBuilder();

    for (ValueBounds argumentBound : argumentBounds) {
      if(!argumentBound.isAttributeConstant()) {
        return null;
      }
      if(argumentBound.getLength() == resultLength) {
        switch (method.getPreserveAttributesStyle()) {
          case ALL:
            attributes.combineFrom(argumentBound.getConstantAttributes());            
            break;
          case STRUCTURAL:
            attributes.combineStructuralFrom(argumentBound.getConstantAttributes());
            break;
        }
      }
    }
    return attributes.build();
  }
  

  public Specialization specializeFurther() {
    if(valueBounds.getLength() == 1) {
      DoubleBinaryOp op = DoubleBinaryOp.trySpecialize(name, method, valueBounds);
      if(op != null) {
        return op;
      }
      return new DataParallelScalarCall(method, argumentTypes, valueBounds).trySpecializeFurther();
    }
    return this;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new UnsupportedOperationException();
  }
}
