package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Reassignment;
import org.renjin.sexp.Symbol;


public class ReassignLeftTranslator extends AssignLeftTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("<<-");
  }
  

  @Override
  protected void doAssignment(IRBodyBuilder builder, LValue target,
      Expression rhs) {
 
    builder.addStatement(new Reassignment((EnvironmentVariable) target, rhs));
    
  }
 

  
}
