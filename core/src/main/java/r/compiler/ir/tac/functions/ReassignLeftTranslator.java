package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tac.statements.Reassignment;
import r.lang.Symbol;

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
