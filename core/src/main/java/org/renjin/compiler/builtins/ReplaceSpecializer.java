package org.renjin.compiler.builtins;

import org.renjin.compiler.builtins.subset.UpdateElementCall;
import org.renjin.compiler.ir.ValueBounds;

import java.util.List;

/**
 * Specializes {@code [<- } calls
 */
public class ReplaceSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(List<ValueBounds> argumentTypes) {
    if(argumentTypes.size() == 3) {
      
      
      ValueBounds inputVector = argumentTypes.get(0);
      ValueBounds subscript = argumentTypes.get(1);
      ValueBounds replacement = argumentTypes.get(2);
      
      if(subscript.getLength() == 1 && replacement.getLength() == 1) {
        return new UpdateElementCall(inputVector, subscript, replacement);
      }
      
    }

    return UnspecializedCall.INSTANCE;
  }
}
