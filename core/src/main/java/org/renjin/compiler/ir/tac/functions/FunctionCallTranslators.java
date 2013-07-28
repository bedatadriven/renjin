package org.renjin.compiler.ir.tac.functions;

import com.google.common.collect.Maps;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SpecialFunction;

import java.util.Map;

public class FunctionCallTranslators {

  private Map<String, FunctionCallTranslator> specials = Maps.newIdentityHashMap();
  
  public FunctionCallTranslators() {
    specials.put("if", new IfTranslator());
    specials.put("{", new BracketTranslator());
    specials.put("(", new ParenTranslator());
    specials.put("<-", new AssignLeftTranslator());
    specials.put("<<-", new ReassignLeftTranslator());
    specials.put("=", new AssignTranslator());
    specials.put("for", new ForTranslator());
    specials.put("repeat", new RepeatTranslator());
    specials.put("while", new WhileTranslator());
    specials.put("next", new NextTranslator());
    specials.put("break", new BreakTranslator());
    specials.put("function", new ClosureTranslator());
    specials.put("$", new DollarTranslator());
    specials.put("$<-", new DollarAssignTranslator());
    specials.put(".Internal", new InternalCallTranslator());
    specials.put("&&", new AndTranslator());
    specials.put("||", new OrTranslator());
    specials.put("switch", new SwitchTranslator());
    specials.put("quote", new QuoteTranslator());
    specials.put("return", new ReturnTranslator());
  }
  
  public FunctionCallTranslator get(PrimitiveFunction function) {
    if(function instanceof SpecialFunction && specials.containsKey(function.getName())) {
      return specials.get(function.getName());
    }
    return BuiltinTranslator.INSTANCE;
  }
}
