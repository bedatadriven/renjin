package org.renjin.stats;

import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.sexp.SEXP;

public class EvalTestCase {

  private Context context;

  @Before
  public void setupR() throws IOException {
    
    
    context = Context.newTopLevelContext();
    context.init();
    
  }
  

  protected SEXP c_i(int... i) {
    throw new UnsupportedOperationException();
  }

  protected SEXP c(double... values) {
    throw new UnsupportedOperationException();
  }

  protected SEXP eval(String source)  {
    SEXP expr;
    try {
      expr = RParser.parseAllSource(new StringReader(source));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return context.evaluate(expr);
  }
  

  protected Matcher<Double> closeTo(double c, double d) {
    throw new UnsupportedOperationException();
  }
  
  
  protected Matcher<SEXP> closeTo(SEXP c, double d) {
    throw new UnsupportedOperationException();

  }

  
}
