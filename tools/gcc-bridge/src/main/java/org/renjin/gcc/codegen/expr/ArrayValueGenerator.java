package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleArrayType;


public interface ArrayValueGenerator extends ValueGenerator {

  @Override
  GimpleArrayType getGimpleType();
  
  Type getComponentType();
  
}
