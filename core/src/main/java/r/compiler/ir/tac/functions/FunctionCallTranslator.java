package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Symbol;

public abstract class FunctionCallTranslator {

  public abstract Symbol getName();
  
  public abstract Operand translateToExpression(IRBlockBuilder builder, TranslationContext context, 
      FunctionCall call);

  public abstract void addStatement(IRBlockBuilder builder, TranslationContext context, FunctionCall call);
 
 
}
