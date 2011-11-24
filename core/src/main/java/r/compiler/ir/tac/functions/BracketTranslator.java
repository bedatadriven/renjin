package r.compiler.ir.tac.functions;


import java.util.Iterator;

import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;

public class BracketTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("{");
  }

  @Override
  public Operand translateToRValue(TacFactory factory, FunctionCall call) {
    if(call.getArguments().length() == 0) {
      return new Constant(Null.INSTANCE);
    } else {
      for(PairList.Node arg : call.getArguments().nodes()) {
        if(arg.hasNextNode()) {
          factory.addStatement(arg.getValue()); 
        } else {
          return factory.translateToRValue(arg.getValue());
        }
      }
      throw new Error("unreachable");
    }
  }

  @Override
  public void addStatement(TacFactory tacFactory, FunctionCall call) {
    if(call.getArguments() != Null.INSTANCE) {
      for(SEXP arg : call.getArguments().values()) {
        tacFactory.addStatement(arg);
      }
    }
  }
}
