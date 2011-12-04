package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.CannotBuildTacException;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class AssignLeftTranslator extends FunctionCallTranslator {

  @Override
  public Operand translateToExpression(TranslationContext context, FunctionCall call) {
    return addAssignment(context, call);
  }
  
  @Override
  public void addStatement(TranslationContext factory, FunctionCall call) {
    addAssignment(factory, call);
  }
  
  private Variable addAssignment(TranslationContext factory, FunctionCall call) {
    if(!(call.getArgument(0) instanceof Symbol)) {
      throw new CannotBuildTacException("complex assignments not yet implemented");
    }
    Variable target = new Variable((Symbol)call.getArgument(0));
    Operand rvalue = factory.translateExpression(call.getArgument(1));
    factory.addStatement(new Assignment(target, rvalue));
    return target;
  }

  @Override
  public Symbol getName() {
    return Symbol.get("<-");
  }
}
