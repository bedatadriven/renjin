package org.renjin.compiler.pipeline;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.concurrent.Executors;


public class DeferredGraphTest extends EvalTestCase {

  @Test
  public void fuseTest() {
    eval("x <- 1:1000");
    eval("y <- 1:1000 / 2");
    SEXP sum = eval("sum(x + y ^ 2)");
    
    DeferredGraph graph = new DeferredGraph((Vector)sum);
    graph.dumpGraph();
    graph.optimize();
    graph.dumpGraph();
    
    
    VectorPipeliner pipeliner = new VectorPipeliner(Executors.newFixedThreadPool(1));
    pipeliner.evaluate(graph);
  }
  
}