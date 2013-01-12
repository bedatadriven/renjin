package org.renjin.maven.test;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import com.google.common.collect.Lists;

public class TestReporter {

  private List<TestSuiteResult> suites = Lists.newArrayList();
  private TestSuiteResult currentSuite;
  private long currentSuiteStarted;
  private File reportsDir;
  
  private TestCaseResult currentCase;
  private long currentCaseStarted;
  
  private PrintWriter stdout;
  
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
    currentSuite.setClassName(stripExt(file));
    currentSuiteStarted = System.currentTimeMillis();
    suites.add(currentSuite);
    stdout = openOutput();
  }

  private String stripExt(File file) {
    String name = file.getName();
    return name.substring(0, name.length() - ".R".length());
  }

  private PrintWriter openOutput() {
    try {
      return new PrintWriter(new File(reportsDir, currentSuite.getClassName() + "-output.txt"));
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  

  public PrintWriter getStdOut() {
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
    
    stdout.println("---------------------------------------------------");
    stdout.println("Running " + name + "()");
    stdout.println("---------------------------------------------------");
    
  }
  
  public void functionSucceeded() {
    currentCase.setOutcome(TestOutcome.SUCCESS);
    functionComplete();
  }
  
  public void functionThrew(Exception e) {
    currentCase.setOutcome(TestOutcome.ERROR);
    currentCase.setException(e);
    System.err.println(String.format("%s() in %s failed: %s", 
        currentCase.getName(), 
        currentSuite.getScriptFile().getName(),
        e.getMessage()));
    e.printStackTrace(stdout);
    e.printStackTrace();
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
}
