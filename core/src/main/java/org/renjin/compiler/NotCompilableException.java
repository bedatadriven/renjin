package org.renjin.compiler;


import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

public class NotCompilableException extends RuntimeException {
  private SEXP sexp;

  public NotCompilableException(SEXP sexp) {
    super("unsupported expression: " + sexp);
    this.sexp = sexp;
  }

  public NotCompilableException(SEXP sexp, String message) {
    super(message);
    this.sexp = sexp;
  }
}
