package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.combine.Combine;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import java.util.List;

/**
 * Specializes calls to {@code c}
 */
public class CombineSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    
    SEXP constantResult = tryCombine(argumentTypes);
    if(constantResult != null) {
      return new ConstantCall(constantResult);
    }
    
    return UnspecializedCall.INSTANCE;
  }

  private SEXP tryCombine(List<ValueBounds> argumentTypes) {
    ListVector.Builder constants = ListVector.newBuilder();
    for (ValueBounds argumentType : argumentTypes) {
      if(argumentType.isConstant()) {
        constants.add(argumentType.getConstantValue());
      } else {
        return null;
      }
    }

    return Combine.c(constants.build(), false);
    
  }

  private boolean allArgumentsAreAtomic(List<ValueBounds> argumentTypes) {
    for (ValueBounds argumentType : argumentTypes) {
      if((argumentType.getTypeSet() & ~TypeSet.ANY_ATOMIC_VECTOR) != 0) {
        return false;
      }
    }
    return true;
  }
  
  private boolean allAreConstant(List<ValueBounds> argumentBounds) {
    for (ValueBounds argumentBound : argumentBounds) {
      if(!argumentBound.isConstant()) {
        return false;
      }
    }
    return true;
  }


}
