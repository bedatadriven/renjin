package org.renjin.primitives.packaging;

import org.renjin.sexp.SEXP;

public abstract class Dataset {
  
  
  public abstract String getName();
  
  public abstract SEXP load();
  
}
