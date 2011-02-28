package r.base;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import r.lang.Context;
import r.lang.Environment;
import r.lang.SEXP;
import r.parser.RParser;

public class BaseLoader {

  public static void load(Context context) {
    Reader reader = new InputStreamReader(BaseLoader.class.getResourceAsStream("/r/library/base/R/base"));
    Environment baseEnvironment = context.getEnvironment().getBaseEnvironment();
    SEXP loadingScript;
    try {
      loadingScript = RParser.parseSource(reader).evaluate(context, 
          baseEnvironment).getExpression();
    } catch (IOException e) {
      throw new RuntimeException("Error loading R base package loading script", e);
    }
    loadingScript.evaluate(context, baseEnvironment);
  }
  
}
