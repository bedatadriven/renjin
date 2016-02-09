package org.renjin.test;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.AtomicVector;

import java.util.List;
import java.util.concurrent.*;

/**
 * Verify that we can safely run multiple sessions independently
 */
public class RngConcurrencyTest {

  public static final int NUM_THREADS = 20;

  private class EvalTask implements Callable<AtomicVector> {
    
    private String code;

    public EvalTask(String code) {
      this.code = code;
    }

    @Override
    public AtomicVector call() throws Exception {
      Session session = new SessionBuilder().build();
      RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
      RenjinScriptEngine scriptEngine = factory.getScriptEngine(session);

      scriptEngine.eval("library(org.renjin.test.dotcall)");
      return (AtomicVector) scriptEngine.eval(code);
    }
  }

  private List<Future<AtomicVector>> evalConcurrently(String code) throws InterruptedException {
    ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
    List<EvalTask> tasks = Lists.newArrayList();
    for(int i=0;i< NUM_THREADS;++i) {
      tasks.add(new EvalTask(code));
    }
    List<Future<AtomicVector>> results = service.invokeAll(tasks);
    service.shutdownNow();
    return results;
  }
  
  
  @Test
  public void testConcurrentRng() throws InterruptedException, ExecutionException {
    
    List<Future<AtomicVector>> results = evalConcurrently("set.seed(1041); mySample(100000);");
    
    // All the results should be identical
    for (int i = 0; i < NUM_THREADS; i++) {
      AtomicVector resultVector = results.get(i).get();
      double lastElement = resultVector.getElementAsDouble(100000-1);
      // Given the seed above, we know what the last seed should be
      if(lastElement != 13) {
        throw new AssertionError("Result from task " + i + " was inconsistent.");
      }
    }
  }
  
  @Test
  public void concurrentGlobals() throws InterruptedException, ExecutionException {
    List<Future<AtomicVector>> results = evalConcurrently("c(global.count(), global.count())");

    for (int i = 0; i < NUM_THREADS; i++) {
      AtomicVector resultVector = results.get(i).get();
      int x1 = resultVector.getElementAsInt(0);
      int x2 = resultVector.getElementAsInt(1);

      System.out.println(resultVector);
      
      System.out.println(i + ": " + x1 + ", " + x2);
      
      if( ! (x1 == 1 && x2 == 2) ) {
        throw new AssertionError("inconsistent result at " + i);
      }
    }
  }
}
