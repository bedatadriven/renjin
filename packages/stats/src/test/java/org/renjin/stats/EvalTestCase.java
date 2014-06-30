package org.renjin.stats;

import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbol;
import org.renjin.primitives.Warning;

public class EvalTestCase {

  protected Context context;

  @Before
  public void setupR() throws IOException {
    
    
    context = Context.newTopLevelContext();
    context.init();
    context.evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get("stats")));
    
    
  }
  

  protected SEXP c_i(int... i) {
    return new IntArrayVector(i);
  }

  protected SEXP c(double... values) {
    return new DoubleArrayVector(values);
  }
  
  protected SEXP c(String... values) {
    return new StringArrayVector(values);
  }

  protected SEXP eval(String source)  {
    SEXP expr;
    try {
      expr = RParser.parseAllSource(new StringReader(source), "inline-string");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return context.evaluate(expr);
  }



  protected final void printWarnings() {
    SEXP warnings = context.getEnvironment().getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      context.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
              context.getEnvironment().getBaseEnvironment());
    }
  }


  
}
