package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.eval.Context;


/**
 * Variable that is to be stored as a JVM local variable when 
 * compiled. During interpretation, it is allocated a slot
 * in the temps array.
 * 
 */
public class LocalVariable extends Variable {
  
  private final String name;
  private final int offset;
 
  public LocalVariable(String name, int offset) {
    super();
    this.name = name;
    this.offset = offset;
  }

  @Override
  public boolean isDefinitelyPure() {
    return false;
  }

  @Override
  public Class getType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LocalVariable other = (LocalVariable) obj;
    
    return name.equals(other.name);
  }

  @Override
  public String toString() {
    return name;
  }

}
