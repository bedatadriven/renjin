package org.renjin.maven.test;

import java.io.*;
import java.util.List;

import org.renjin.eval.EvalException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class TestReporter {

  private List<TestSuiteResult> suites = Lists.newArrayList();
  private TestSuiteResult currentSuite;
  private long currentSuiteStarted;
  private File reportsDir;
  
  private TestCaseResult currentCase;
  private long currentCaseStarted;
  
  private PrintStream stdout;
  
  
  public TestReporter(File reportsDir) {
    this.reportsDir = reportsDir;
    this.reportsDir.mkdirs();
  }
  
  public void start() {
    System.out.println("-------------------------------------------------------");
    System.out.println(" R E N J I N   T E S T S");
    System.out.println("-------------------------------------------------------");  
  }
  
  public void startFile(File file) {
    System.out.println("Running " + file.getName());
    
    currentSuite = new TestSuiteResult();
    currentSuite.setScriptFile(file);
    currentSuite.setClassName(suiteName(file));
    currentSuiteStarted = System.currentTimeMillis();
    suites.add(currentSuite);
    stdout = openTestOutput();
  }

  private String suiteName(File file) {
    String name = file.getName();
    if(name.endsWith(".R")) {
      return name.substring(0, name.length() - ".R".length());
    } else if(name.endsWith(".Rd")) {
      return name.substring(0, name.length() - ".Rd".length()) + "-examples";
    } else {
      return name;
    }
  }

  private PrintStream openTestOutput() {
    try {
      return new PrintStream(
          new CappedOutputStream(new FileOutputStream(
           new File(reportsDir, currentSuite.getClassName() + "-output.txt"))));
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  

  public PrintStream getStdOut() {
    return stdout;
  }
  
  public void fileComplete() {
    currentSuite.setTime( (System.currentTimeMillis() - currentSuiteStarted) / 1000d );  
    currentSuite.writeXml(reportsDir);
    
    stdout.close();
    
    printResultsBanner(System.out);
   
  }


  private void printResultsBanner(PrintStream out) {
    out.println(String.format("Tests run: %d, Failures: %d, Errors: %d, Skipped: %d, Time elapsed: %.3f %s",
        currentSuite.getResults().size(),
        currentSuite.countOutcomes(TestOutcome.FAILURE),
        currentSuite.countOutcomes(TestOutcome.ERROR),
        currentSuite.countOutcomes(TestOutcome.SKIPPED),
        currentSuite.getTime(),
        currentSuite.hasFailures() ? " << FAILURE!" : ""));
  }
  
  public void startFunction(String name) {
    if(name.startsWith("test.")) {
      name = name.substring("test.".length());
    }
    
    currentCase = new TestCaseResult();
    currentCase.setClassName(currentSuite.getClassName());
    currentCase.setName(name);
    currentSuite.addCase(currentCase);
    currentCaseStarted = System.currentTimeMillis();
  }
  
  public void functionSucceeded() {
    currentCase.setOutcome(TestOutcome.SUCCESS);
    functionComplete();
  }
  
  public void functionThrew(Exception e)  {
    currentCase.setOutcome(TestOutcome.ERROR);
    currentCase.setException(e);
    System.err.println(String.format("%s() in %s failed: %s", 
        currentCase.getName(), 
        currentSuite.getScriptFile().getName(),
        e.getMessage()));
    if(e instanceof EvalException) {
      ((EvalException) e).printRStackTrace(stdout);
      ((EvalException) e).printRStackTrace(System.err);
    } else {
      e.printStackTrace(stdout);
      e.printStackTrace();
    }
    functionComplete();
  }

  private void functionComplete() {
    currentSuite.setTime( (System.currentTimeMillis() - currentCaseStarted) / 1000d );
  }

  public boolean allTestsSucceeded() {
    for(TestSuiteResult suite : suites) {
      if(suite.hasFailures()) {
        return false;
      }
    }
    return true;
  }

  public PrintWriter getStdOutWriter() {
    OutputStreamWriter writer = new OutputStreamWriter(stdout, Charsets.UTF_8);
    return new PrintWriter(writer, true);
  } 
}
