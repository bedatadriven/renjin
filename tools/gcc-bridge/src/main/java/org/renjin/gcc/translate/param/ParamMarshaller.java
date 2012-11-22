package org.renjin.gcc.translate.param;

import org.renjin.gcc.translate.var.Variable;

public abstract class ParamMarshaller {

  public abstract boolean accept(Variable variable, CallParam param);
  
}
