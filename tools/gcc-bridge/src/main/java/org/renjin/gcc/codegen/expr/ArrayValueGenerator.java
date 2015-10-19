package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;


public interface ArrayValueGenerator extends ValueGenerator {
  
  Type getComponentType();
  
}
