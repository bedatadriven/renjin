package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Reassignment;


public class ReassignLeftTranslator extends AssignLeftTranslator {


  @Override
  protected void doAssignment(IRBodyBuilder builder, LValue target,
      Expression rhs) {
 
    builder.addStatement(new Reassignment((EnvironmentVariable) target, rhs));
    
  }
 

  
}
