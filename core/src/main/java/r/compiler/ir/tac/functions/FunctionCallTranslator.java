package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.Expr;
import r.compiler.ir.tac.TacFactory;
import r.lang.FunctionCall;
import r.lang.Symbol;

public abstract class FunctionCallTranslator {

  public abstract Symbol getName();
  
  public abstract Expr translateToRValue(TacFactory factory, FunctionCall call);

  public abstract void addStatement(TacFactory tacFactory, FunctionCall exp);
 
 
}
