package org.renjin.compiler.ir.tac;

public class IRLabel {
  
  private final int index;
  
  public IRLabel(int index) {
    this.index = index;
  }
  
  public int getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "L" + index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + index;
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
    IRLabel other = (IRLabel) obj;
    if (index != other.index)
      return false;
    return true;
  }
}
