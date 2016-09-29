package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Attributes;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.List;

/**
 * Specializes calls to the {@code dim} primitive
 */
public class DimSpecializer implements Specializer {

  private JvmMethod method;

  public DimSpecializer() {
    this.method = Iterables.getOnlyElement(JvmMethod.findOverloads(Attributes.class, "dim", null));
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    if(argumentTypes.size() != 1) {
      throw new InvalidSyntaxException("dim() takes one argument.");
    }
    ValueBounds sexp = argumentTypes.get(0);
    
    if(sexp.isDimAttributeConstant()) {
      return new ConstantCall(sexp.getConstantDimAttribute());
    }
    
    return new StaticMethodCall(method);
  }
}
