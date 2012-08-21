package org.renjin.benchmarks;

import org.renjin.sexp.StringArrayVector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class Benchmarks {
  
  
  public static void main(String[] args) throws IOException, ScriptException {
  
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("Renjin");

    engine.put("benchmarkArgs", new StringArrayVector(args));

    engine.eval(new InputStreamReader(new FileInputStream("src/main/R/runner.R")));
  }
  
}
