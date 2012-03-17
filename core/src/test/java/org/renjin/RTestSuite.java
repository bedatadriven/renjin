package org.renjin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.script.ScriptException;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import com.google.common.collect.Lists;

public class RTestSuite extends ParentRunner<RTestFile>{

  private List<RTestFile> files = Lists.newArrayList();
  private RenjinScriptEngine engine;
  
  public RTestSuite(Class<?> testClass) throws InitializationError {
    super(testClass);

    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    engine = factory.getScriptEngine();
    try {
      engine.eval(new File("src/test/R/hamcrest.R"));
    } catch (Exception e1) {
      throw new InitializationError(e1);
    }
    
    File sourceRoot = new File("src/test/R");
    for(File sourceFile : sourceRoot.listFiles()) {
      if(sourceFile.getName().endsWith(".R")) {
        try {
          files.add(new RTestFile(engine, sourceFile));
        } catch (IOException e) {
          throw new InitializationError(e);
        }
      }
    }
    
  }

  @Override
  protected List<RTestFile> getChildren() {
    return files;
  }

  @Override
  protected Description describeChild(RTestFile child) {
    return child.getDescription();
  }

  @Override
  protected void runChild(RTestFile child, RunNotifier notifier) {
    child.run(notifier);
  }

}
