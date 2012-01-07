package r.compiler.ir.tac.functions;

import r.lang.Symbol;

public class ReassignLeftTranslator extends AssignLeftTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("<<-");
  }
  
 

  
}
