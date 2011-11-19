package r.benchmarks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Benchmarks {
  
  
  public static void main(String[] args) throws IOException, ScriptException {
  
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("Renjin");

    engine.eval(new InputStreamReader(new FileInputStream("src/main/R/runner.R")));
  }
  
}
