package org.renjin.gcc.gimple.type;

import org.objectweb.asm.Type;

public abstract class GimplePrimitiveType extends AbstractGimpleType {

  /**
   * @return the number of slots required to store this type on the stack or 
   * in the local variable table in the JVM.
   */
  public abstract int localVariableSlots();

  /**
   * 
   * @return the equivalent JVM type
   */
  public abstract Type jvmType();
}
