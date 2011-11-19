package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.CannotBuildTacException;
import r.compiler.ir.tac.Expr;
import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.Variable;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class AssignLeftTranslator extends FunctionCallTranslator {

  @Override
  public Expr translateToRValue(TacFactory factory, FunctionCall call) {
    return addAssignment(factory, call);
  }
  
  @Override
  public void addStatement(TacFactory factory, FunctionCall call) {
    addAssignment(factory, call);
  }
  

  private Variable addAssignment(TacFactory factory, FunctionCall call) {
    if(!(call.getArgument(0) instanceof Symbol)) {
      throw new CannotBuildTacException("complex assignments not yet implemented");
    }
    Variable target = new Variable((Symbol)call.getArgument(0));
    Expr rvalue = factory.translateToRValue(call.getArgument(1));
    factory.addAssignment(target, rvalue);
    return target;
  }

  @Override
  public Symbol getName() {
    return Symbol.get("<-");
  }

}
