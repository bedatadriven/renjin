package org.renjin.benchmarks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.renjin.sexp.StringVector;


public class Benchmarks {
  
  
  public static void main(String[] args) throws IOException, ScriptException {
  
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("Renjin");

    engine.put("benchmarkArgs", new StringVector(args));
    
    engine.eval(new InputStreamReader(new FileInputStream("src/main/R/runner.R")));
  }
  
}
