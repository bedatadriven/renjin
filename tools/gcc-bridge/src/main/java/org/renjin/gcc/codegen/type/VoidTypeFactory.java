package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.ret.VoidReturnGenerator;

/**
 * Creates generators for void value types. Only used for return types.
 */
public class VoidTypeFactory extends TypeFactory {

  @Override
  public ReturnGenerator returnGenerator() {
    return new VoidReturnGenerator();
  }
}
