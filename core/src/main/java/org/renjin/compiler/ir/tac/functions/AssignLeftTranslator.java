package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.eval.EvalException;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


public class AssignLeftTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {


    Expression rhs = builder.translateExpression(context, call.getArgument(1));

    // since the rhs will be used as this expression's value, 
    // we need to assure that it is not evaluated twice
    if(!(rhs instanceof Constant)) {
      // avoid evaluating RHS twice
      Temp temp = builder.newTemp();
      builder.addStatement(new Assignment(temp, rhs));
      rhs = temp;
    }
    
    addAssignment(builder, context, call, rhs);
    return rhs;
  }
  
  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall assignment) {
    Expression rhs = builder.translateExpression(context, assignment.getArgument(1));

    addAssignment(builder, context, assignment, rhs);
  }
  
  private void addAssignment(IRBodyBuilder builder, TranslationContext context, FunctionCall assignment, 
      Expression rhs) {
    // this loop handles nested, complex assignments, such as:
    // class(x) <- "foo"
    // x$a[3] <- 4
    // class(x$a[3]) <- "foo"

    SEXP lhs = assignment.getArgument(0);
    
    while(lhs instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) lhs;
      
      rhs = builder.translateSetterCall(context, call, rhs);
      lhs = call.getArgument(0);
    }

    LValue target;
    if( lhs instanceof Symbol) {
      target = new EnvironmentVariable((Symbol) lhs);
    } else if(lhs instanceof StringVector) {
      target =  new EnvironmentVariable( Symbol.get(((StringVector) lhs).getElementAsString(0)) );
    } else {
      throw new EvalException("cannot assign to value of type " + lhs.getTypeName());
    }

    doAssignment(builder, target, rhs);
    
  }

  protected void doAssignment(IRBodyBuilder builder, LValue target, Expression rhs) {
    // make the final assignment to the target symbol
    builder.addStatement(new Assignment(target, rhs));
  }

  @Override
  public Symbol getName() {
    return Symbol.get("<-");
  }
}
