package org.renjin.stats;

import org.junit.Test;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;

public class StatsTest {

  @Test
  public void test() throws Exception {
    
    Context context = Context.newTopLevelContext();
    context.init();
    context.evaluate(RParser.parseSource("library(stats)\n"));
   
    context.evaluate(RParser.parseSource("print(search())\n"));
    context.evaluate(RParser.parseSource("print(runif(5))\n"));
    context.evaluate(RParser.parseSource("print(environment(runif))\n"));
    
  }
  
}
