package org.renjin.compiler.ir.tac;

import org.renjin.eval.Context;
import org.renjin.sexp.Closure;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;


public class IRFunction {
  private static int nextDebugId = 1;
  
  private final PairList formals;
  private final SEXP body;
  private final IRBody block;
  private final int debugId;
  

  public IRFunction(PairList formals, SEXP body, IRBody block) {
    super();
    this.formals = formals;
    this.body = body;
    this.block = block;
    this.debugId = (nextDebugId++);
  }
  
  public int getId() {
    return debugId;
  }
  
  public IRBody getBody() {
    return block;
  }

  public PairList getFormals() {
    return formals;
  }
  
  public Closure newClosure(Context context) {
    return new IRClosure(context.getEnvironment(), this);
  }
  
  public SEXP getBodyExpression() {
    return body;
  }
  
  @Override
  public String toString() {
    return "function@" + debugId;
  }

}
