package org.renjin.maven.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.repl.JlineRepl;
import org.renjin.sexp.Closure;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Discovers and runs a set of R test scripts, usually from
 * src/test/R or tests/ or man/
 * 
 */
public class TestRunner {

  private TestReporter reporter;
  private List<String> defaultPackages = Lists.newArrayList();
  private String namespaceUnderTest;

  public TestRunner(String namespaceUnderTest, File reportsDirectory, List<String> defaultPackages) {
    this.namespaceUnderTest = namespaceUnderTest;
    this.defaultPackages = defaultPackages;
    
    reporter = new TestReporter(reportsDirectory);
    reporter.start();
  }
  
  public boolean run(File sourceDirectory) throws Exception {
    if(sourceDirectory.isDirectory() && sourceDirectory.listFiles() != null) {
      for(File sourceFile : sourceDirectory.listFiles()) {
        if(sourceFile.getName().toUpperCase().endsWith(".R")) {
          executeFile(sourceFile, Files.toString(sourceFile, Charsets.UTF_8));
        } else if(sourceFile.getName().endsWith(".Rd")) {
          executeFile(sourceFile, ExamplesParser.parseExamples(sourceFile));
        }
      }
    }
    
    return reporter.allTestsSucceeded();
  }

  private void executeFile(File sourceFile, String source) {
    try {
      reporter.startFile(sourceFile);
      executeTestFile(sourceFile, source);
      reporter.fileComplete();
    } catch (IOException e) {
      System.out.println("FAILURE: " + sourceFile.getName());
    }
  }
  
  private Session createSession(File workingDir) throws IOException  {

    Session session = SessionBuilder.buildDefault();
    session.setWorkingDirectory(
        session.getFileSystemManager()
          .resolveFile(workingDir.toURI().toString()));
    
    session.setStdErr(reporter.getStdOutWriter());
    session.setStdOut(reporter.getStdOutWriter());
    
    if(defaultPackages.isEmpty()) {
      System.err.println("No default packages specified");
    }
    
    for(String pkg : defaultPackages) {
      System.err.println("Loading default package " + pkg);
      loadLibrary(session, pkg);
    }
    return session;
  }

  private void loadLibrary(Session session, String namespaceName) {
    try {
      session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get(namespaceName)));
    } catch(Exception e) {
      System.err.println("Could not load this project's namespace (it may not have one)");
      //e.printStackTrace();
    }
  }
  

  private boolean isZeroArgFunction(SEXP value) {
    if(value instanceof Closure) {
      Closure testFunction = (Closure)value;
      if(testFunction.getFormals().length() == 0) {
        return true;
      }
    } 
    return false;
  }

  private void executeTestFile(File sourceFile, String sourceText) throws IOException {
    
    if(sourceText.trim().isEmpty()) {
      // skip empty files or Rd docs with no examples
      return;
    }
    
    reporter.startFunction("root");
    
    Session session = createSession(sourceFile.getParentFile());

    // Examples assume that the package is already on the search path
    if(sourceFile.getName().endsWith(".Rd")) {
      loadLibrary(session, namespaceUnderTest);
    }
    
    UnsupportedTerminal term = new UnsupportedTerminal();
    InputStream in = new ByteArrayInputStream(sourceText.getBytes(Charsets.UTF_8));
    ConsoleReader consoleReader = new ConsoleReader(in, reporter.getStdOut(), term);
    JlineRepl repl = new JlineRepl(session, consoleReader);
    repl.setEcho(true);
    repl.setStopOnError(true);

    try {
      repl.run();
      reporter.functionSucceeded();
    
    } catch(Exception e) {
      reporter.functionThrew(e);
      return;
    }
    
    // look for "junit-style" test functions.
    // This is renjin's own convention, but it's nice to be
    // able to see the results of many tests rather than 
    // topping at the first error
    for(Symbol name : session.getGlobalEnvironment().getSymbolNames()) {
      if(name.getPrintName().startsWith("test.")) {
        SEXP value = session.getGlobalEnvironment().getVariable(name);
        if(isZeroArgFunction(value)) {
          executeTestFunction(session.getTopLevelContext(), name);
        }
      }
    }
  }


  private void executeTestFunction(Context context, Symbol name) {
    try {
      reporter.startFunction(name.getPrintName());
      context.evaluate(FunctionCall.newCall(name));
      reporter.functionSucceeded();
    } catch(Exception e) {
      reporter.functionThrew(e);
    }
  }
}