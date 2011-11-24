package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Symbol;

public abstract class FunctionCallTranslator {

  public abstract Symbol getName();
  
  public abstract Operand translateToRValue(TacFactory factory, FunctionCall call);

  public abstract void addStatement(TacFactory tacFactory, FunctionCall exp);
 
 
}
