package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;

public class NamespaceDef {

  private List<String> imports = Lists.newArrayList();
  private List<String> exports = Lists.newArrayList();
  
  public void parse(Reader reader) throws IOException {
    parse(RParser.parseAllSource(reader));
  }

  private void parse(ExpressionVector vector) {
    for(SEXP exp : vector) {
      FunctionCall call = (FunctionCall)exp;
      if(call.getFunction() == Symbol.get("export")) {
        addTo(call.getArguments(), exports);
        
      } else if(call.getFunction() == Symbol.get("import")) {
        addTo(call.getArguments(), imports);
        
      } else if(call.getFunction() == Symbol.get("S3Method")) {
        
      }
    }
  }
  
  private void addTo(PairList arguments, List<String> list) {
    for(SEXP exp : arguments.values()) {
      Symbol name = (Symbol) exp;
      list.add(name.getPrintName());
    }
  }
}
