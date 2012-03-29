package org.renjin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;


public class RTestFile extends ParentRunner<RTestFunction> {

  private List<RTestFunction> tests = Lists.newArrayList();
  private File sourceFile;
  
  public RTestFile(RenjinScriptEngine engine, File sourceFile) throws InitializationError, IOException {
    super(RTestFile.class);
    this.sourceFile = sourceFile;
    
    // Create a new environment in which to run these tests
    Context context = engine.getRuntimeContext();
    Environment testEnvironment = Environment.createChildEnvironment(context.getGlobalEnvironment());
    Context testContext = context.beginEvalContext(testEnvironment);
    
    // Parse script
    Reader reader = new InputStreamReader(new FileInputStream(sourceFile));
    SEXP testExp = RParser.parseAllSource(reader);
    reader.close();
    
    // evaluate in environment
    testContext.evaluate(testExp);
    
    // find tests
    for(Symbol name : testEnvironment.getSymbolNames()) {
      if(name.getPrintName().startsWith("test") &&
           testEnvironment.getVariable(name) instanceof Closure) {
        tests.add(new RTestFunction(testContext, name));
      }
    }
  }
  
  @Override
  protected String getName() {
    return sourceFile.getName();
  }

  @Override
  protected List<RTestFunction> getChildren() {
    return tests;
  }

  @Override
  protected Description describeChild(RTestFunction child) {
    return child.getDescription();
  }

  @Override
  protected void runChild(RTestFunction child, RunNotifier notifier) {
    child.run(notifier);
  }

}
