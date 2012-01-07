package r.compiler.ir.tac.functions;

import java.util.Map;

import r.lang.SEXP;
import r.lang.Symbol;

import com.google.common.collect.Maps;

public class FunctionCallTranslators {

  private Map<Symbol, FunctionCallTranslator> builders = Maps.newIdentityHashMap();
  
  public FunctionCallTranslators() {
    builders.put(Symbol.get("if"), new IfTranslator());
    builders.put(Symbol.get("{"), new BracketTranslator());
    builders.put(Symbol.get("("), new ParenTranslator());
    builders.put(Symbol.get("<-"), new AssignLeftTranslator());
    builders.put(Symbol.get("for"), new ForTranslator());
    builders.put(Symbol.get("repeat"), new RepeatTranslator());
    builders.put(Symbol.get("while"), new WhileTranslator());
    builders.put(Symbol.get("next"), new NextTranslator());
    builders.put(Symbol.get("break"), new BreakTranslator());
    builders.put(Symbol.get("function"), new ClosureTranslator());
    builders.put(Symbol.get("$"), new DollarTranslator());
    builders.put(Symbol.get("$<-"), new DollarAssignTranslator());
    builders.put(Symbol.get(".Internal"), new InternalCallTranslator());
  }
  
  public FunctionCallTranslator get(SEXP function) {
    if(function instanceof Symbol) {
      if(builders.containsKey(function)) {
        return builders.get(function);
      } else {
        return null;
      }
    } else {
      throw new UnsupportedOperationException(function.toString());
    }
  }
}
