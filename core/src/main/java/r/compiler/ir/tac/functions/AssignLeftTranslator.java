package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.CannotBuildTacException;
import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class AssignLeftTranslator extends FunctionCallTranslator {

  @Override
  public Operand translateToExpression(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    return addAssignment(builder, context, call);
  }
  
  @Override
  public void addStatement(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    addAssignment(builder, context, call);
  }
  
  private Variable addAssignment(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    if(!(call.getArgument(0) instanceof Symbol)) {
      throw new CannotBuildTacException("complex assignments not yet implemented");
    }
    Variable target = new Variable((Symbol)call.getArgument(0));
    Operand rvalue = builder.translateExpression(context, call.getArgument(1));
    builder.addStatement(new Assignment(target, rvalue));
    return target;
  }

  @Override
  public Symbol getName() {
    return Symbol.get("<-");
  }
}
