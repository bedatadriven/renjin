package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;


public class LengthSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    if(argumentTypes.size() != 1) {
      throw new InvalidSyntaxException("length() takes one argument.");
    }

    ValueBounds argumentBounds = argumentTypes.get(0);
    if(argumentBounds.isLengthConstant()) {
      return new ConstantCall(argumentBounds.getLength());
    }
    
    return new LengthCall();
  }
}
