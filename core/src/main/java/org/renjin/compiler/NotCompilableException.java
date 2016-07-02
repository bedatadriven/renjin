package org.renjin.compiler;


import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

public class NotCompilableException extends RuntimeException {
  private SEXP sexp;

  public NotCompilableException(SEXP sexp) {
    super("unsupported expression: " + sexp);
    this.sexp = sexp;
  }

  public NotCompilableException(SEXP sexp, String message) {
    super(sexp + ": " + message);
    this.sexp = sexp;
  }

  public NotCompilableException(FunctionCall call, NotCompilableException cause) {
    super(" in " + call.getFunction(), cause);
  }

  public SEXP getSexp() {
    return sexp;
  }
}
