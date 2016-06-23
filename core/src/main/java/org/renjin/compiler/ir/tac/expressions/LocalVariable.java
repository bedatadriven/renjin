package org.renjin.compiler.ir.tac.expressions;


/**
 * Variable that is to be stored as a JVM local variable when 
 * compiled. During interpretation, it is allocated a slot
 * in the temps array.
 * 
 */
public class LocalVariable extends Variable {
  
  private final String name;

  public LocalVariable(String name) {
    super();
    this.name = name;
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LocalVariable other = (LocalVariable) obj;
    
    return name.equals(other.name);
  }

  @Override
  public String toString() {
    return name;
  }

}
