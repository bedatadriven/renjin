package org.renjin.compiler.ir.ssa;

import java.util.Collections;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.IRUtils;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.eval.Context;


public class SsaVariable extends Variable {
  private final Variable inner;
  private final int version;
  
  public SsaVariable(Variable inner, int version) {
    super();
    if(inner instanceof SsaVariable) {
      throw new IllegalArgumentException("SSA variables should not be nested");
    }
    this.inner = inner;
    this.version = version;
  }
  
  public Variable getInner() {
    return inner;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public boolean isDefinitelyPure() {
    if(version == 0) {
      // version zero is fetched from the environment, which may force
      // a promise with side effects, so we can't trust it.
      return false;
    } else {
      // otherwise we know that we're not dealing with promises
      return true;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(inner.toString());
    
    IRUtils.appendSubscript(sb, version);
    
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + inner.hashCode();
    result = prime * result + version;
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
    SsaVariable other = (SsaVariable) obj;
    return inner.equals(other.inner) && version == other.version;
  }
}
