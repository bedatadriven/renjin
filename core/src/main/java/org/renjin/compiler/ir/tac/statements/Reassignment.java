package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class Reassignment extends Assignment {
 
  public Reassignment(EnvironmentVariable lhs, Expression rhs) {
    super(lhs, rhs);
  }
  
 }
