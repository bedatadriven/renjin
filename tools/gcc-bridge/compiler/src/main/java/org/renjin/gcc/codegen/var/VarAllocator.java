package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Type;

/**
 * Common interface to generating code for local and global variables.
 *
 * @see LocalVarAllocator
 * @see GlobalVarAllocator
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
