package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.List;

/**
 * Specializes calls to the {@code is.array} primitive.
 */
public class IsArraySpecializer implements Specializer {
  
  private JvmMethod method;

  public IsArraySpecializer() {
    this.method = Iterables.getOnlyElement(JvmMethod.findOverloads(Types.class, "is.array", null));
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    if(argumentTypes.size() != 1) {
      throw new InvalidSyntaxException("is.array() takes one argument.");
    }

    ValueBounds argumentBounds = argumentTypes.get(0);
    if(argumentBounds.isDimCountConstant()) {
      return new ConstantCall(argumentBounds.getConstantDimCount() > 0);
    }

    return new StaticMethodCall(method);
  }
}
