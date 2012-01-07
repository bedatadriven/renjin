package r.compiler.ir.tac.functions;


import java.util.List;

import com.google.common.collect.Lists;

import r.compiler.ir.tac.IRScopeBuilder;
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
  public Expression translateToExpression(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    return addAssignment(builder, context, call);
  }
  
  @Override
  public void addStatement(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    addAssignment(builder, context, call);
  }
  
  private Expression addAssignment(IRScopeBuilder builder, TranslationContext context, FunctionCall assignment) {
    // this loop handles nested, complex assignments, such as:
    // class(x) <- "foo"
    // x$a[3] <- 4
    // class(x$a[3]) <- "foo"

    SEXP lhs = assignment.getArgument(0);
    Expression rhs = builder.translateExpression(context, assignment.getArgument(1));
    
    if(isComplex(lhs) && !(rhs instanceof Constant)) {
      // avoid evaluating RHS twice
      Temp temp = builder.newTemp();
      builder.addStatement(new Assignment(temp, rhs));
      rhs = temp;
    }
      
    Expression initialRhs = rhs;

    
    while(lhs instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) lhs;
      Symbol getter = (Symbol) call.getFunction();
      Symbol setter = Symbol.get(getter.getPrintName() + "<-");
      FunctionCall setterCall = new FunctionCall(setter, call.getArguments());
      
      rhs = builder.translateSetterCall(context, setterCall, rhs);
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

    // make the final assignment to the target symbol
    builder.addStatement(new Assignment(target, rhs));
    
    return initialRhs;
  }

  private boolean isComplex(SEXP lhs) {
    return lhs instanceof FunctionCall;
  }

  @Override
  public Symbol getName() {
    return Symbol.get("<-");
  }
}
