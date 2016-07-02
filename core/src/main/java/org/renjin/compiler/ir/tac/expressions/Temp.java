package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.ir.IRFormatting;


/**
 * A slot for a temporary value. 
 * A temporary value can only be assigned once, so it is not
 * have to be processed by the SSA transformation
 */
public class Temp extends LValue {
  
  private static final String TAO = "τ";
  
  private final int index;
  
  public Temp(int index) {
    this.index = index;
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder(TAO);
    IRFormatting.appendSubscript(sb, index+1);
    return sb.toString();
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int hashCode() {
    return index;
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
    Temp other = (Temp) obj;
    return index == other.index;
  }
}
