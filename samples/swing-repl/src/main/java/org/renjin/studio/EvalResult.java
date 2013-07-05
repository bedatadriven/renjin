package org.renjin.studio;

import org.renjin.sexp.SEXP;

public class EvalResult {
  private SEXP sexp;
  private boolean visible;
  
  public EvalResult(SEXP sexp, boolean visible) {
    super();
    this.sexp = sexp;
    this.visible = visible;
  }

  public SEXP getSexp() {
    return sexp;
  }

  public boolean isVisible() {
    return visible;
  }
 
  
}
