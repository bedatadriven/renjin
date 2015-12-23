package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;

/**
 * Created by alex on 22-12-15.
 */
public abstract class VarAllocator {
  
  public abstract Var reserve(String name, Type type);

  public final Var reserve(String name, Class type) {
    return reserve(name, Type.getType(type));
  }

  public final Var reserveArrayRef(String name, Type componentType) {
    return reserve(name, Type.getType("[" + componentType.getDescriptor()));
  }

  public final Var reserveInt(String name) {
    return reserve(name, Type.INT_TYPE);
  }
}
