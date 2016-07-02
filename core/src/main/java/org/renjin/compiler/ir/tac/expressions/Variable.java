package org.renjin.compiler.ir.tac.expressions;


import com.google.common.collect.Maps;
import org.renjin.compiler.ir.ssa.SsaVariable;

import java.util.Map;

public abstract class Variable extends LValue {

  private Map<Integer, SsaVariable> versions = Maps.newHashMap();

  public SsaVariable getVersion(int versionNumber) {
    SsaVariable ssa = versions.get(versionNumber);
    if(ssa == null) {
      ssa = new SsaVariable(this, versionNumber);
      versions.put(versionNumber, ssa);
    }
    return ssa;
  }
}
