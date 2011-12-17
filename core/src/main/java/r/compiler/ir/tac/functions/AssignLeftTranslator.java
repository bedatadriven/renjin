package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.CannotBuildTacException;
import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.Variable;
import r.compiler.ir.tac.statements.Assignment;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class AssignLeftTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    return addAssignment(builder, context, call);
  }
  
  @Override
  public void addStatement(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    addAssignment(builder, context, call);
  }
  
  private Variable addAssignment(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    if(!(call.getArgument(0) instanceof Symbol)) {
      throw new CannotBuildTacException("complex assignments not yet implemented");
    }
    Variable target = new EnvironmentVariable((Symbol)call.getArgument(0));
    Expression rvalue = builder.translateExpression(context, call.getArgument(1));
    builder.addStatement(new Assignment(target, rvalue));
    return target;
  }

  @Override
  public Symbol getName() {
    return Symbol.get("<-");
  }
}
