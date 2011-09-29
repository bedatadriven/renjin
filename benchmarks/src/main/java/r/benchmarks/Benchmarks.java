package r.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import r.lang.Context;
import r.lang.EvalResult;
import r.lang.SEXP;
import r.parser.RParser;

public class Benchmarks {
  
  
  public static void main(String[] args) throws IOException {
  
    long startUpTime = System.nanoTime();
    
    
    Context topLevelContext = Context.newTopLevelContext();
    topLevelContext.init();
    
    long afterInit = System.nanoTime();
    
    InputStream in = Benchmarks.class.getResourceAsStream("mean-var-online.R");
    SEXP source = RParser.parseSource(new InputStreamReader(in));
    
    long afterParse = System.nanoTime();
    
    EvalResult result = source.evaluate(topLevelContext, topLevelContext.getEnvironment());
    System.out.println( result );
    
    long afterEvaluate = System.nanoTime();
    

    System.out.println("Total time:\t" + formatNanos(afterEvaluate - startUpTime));
    System.out.println("Parse time:\t" + formatNanos(afterParse - startUpTime));

    System.out.println("Eval time:\t" + formatNanos(afterEvaluate - afterParse));
        
  }

  private static String formatNanos(long nanos) {
    return (nanos * 1e-9) + " s";
  }
  
}
