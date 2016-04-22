import org.junit.Test;
import org.renjin.sexp.DoubleArrayVector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MultithreadInitTest {

  private static DoubleArrayVector args = new DoubleArrayVector(1,2,3,4,5);

  static class RunClazz implements Runnable {

    @Override
    public void run() {
      System.out.println("thread ID: " + Thread.currentThread().getId());
      try {
        ScriptEngineManager factory = new ScriptEngineManager();

        ScriptEngine engine = factory.getEngineByName("Renjin");

        engine.put("x", args);
        engine.eval("y <- mean(x)");
        Object result = engine.get("y");

        assertThat(((DoubleArrayVector)result).get(0), equalTo(3d));
      } catch(Exception e) {
        e.printStackTrace(new PrintStream(System.err));
      }
    }
  }

  @Test
  public void test() throws InterruptedException {
    int nThreads = 10;
    Thread[] workers = new Thread[nThreads];
    for(int i = 0; i < nThreads; i++) {
      workers[i] = new Thread(new RunClazz());
      workers[i].start();
    }

    for(int i = 0; i < nThreads; i++) {
      workers[i].join();
    }

  }

}
