package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.IRUtils;
import org.renjin.eval.Context;


/**
 * A slot for a temporary value. 
 * A temporary value can only be assigned once, so it is not
 * have to be processed by the SSA transformation
 */
public class Temp implements LValue {
  private static final String TAO = "\u03C4";
  private final int index;
  
  public Temp(int index) {
    this.index = index;
  }
  
  public int getIndex() {
    return index;
  }
  
  private Object retrieveValue(Context context, Object[] temps) {
    return temps[index];
  }
  
  @Override
  public void setValue(Context context, Object[] temp, Object value) {
    temp[index] = value; 
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder(TAO);
    IRUtils.appendSubscript(sb, index+1);
    return sb.toString();
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class inferType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return index;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Temp other = (Temp) obj;
    return index == other.index;
  }

}
