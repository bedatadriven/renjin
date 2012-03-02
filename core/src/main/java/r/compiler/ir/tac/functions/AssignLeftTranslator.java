package r.compiler.ir.tac.functions;


import java.util.List;

import com.google.common.collect.Lists;

import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.expressions.Variable;
import r.compiler.ir.tac.statements.Assignment;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.exception.EvalException;

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
      target =  new EnvironmentVariable( Symbol.get(((StringVector) lhs).getElement(0)) );
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
