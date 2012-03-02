package r.compiler.ir.tac.statements;

import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.lang.Context;
import r.lang.Environment;
import r.lang.SEXP;
import r.lang.Symbol;

public class Reassignment extends Assignment {
 
  public Reassignment(EnvironmentVariable lhs, Expression rhs) {
    super(lhs, rhs);
  }
 
 
  @Override
  public Object interpret(Context context, Object[] temp) {
    Symbol target = ((EnvironmentVariable) getLHS()).getName();
    SEXP rhs = (SEXP) getRHS().retrieveValue(context, temp);
    Environment rho = context.getEnvironment();

    for(Environment env : context.getEnvironment().selfAndParents()) {
      if(env.hasVariable(target))  {
        env.setVariable(target, rhs);
        return null;
      }
    }

    // not defined anywhere we can see, define it anew in the current env
    rho.setVariable(target, rhs);

    return null;
  }
  
 }
