package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;

import java.util.List;


public class LengthSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(List<ValueBounds> argumentTypes) {
    return null;
  }
}
