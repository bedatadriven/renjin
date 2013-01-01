package org.renjin.maven.test;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

public class TestReporter {

  private List<TestSuiteResult> suites = Lists.newArrayList();
  private TestSuiteResult currentSuite;
  private long currentSuiteStarted;
  private File reportDir = new File("target/surefire-reports");
  
  private TestCaseResult currentCase;
  private long currentCaseStarted;
  
  public void start() {
    System.out.println("-------------------------------------------------------");
    System.out.println(" R E N J I N   T E S T S");
    System.out.println("-------------------------------------------------------");  
  }
  
  public void startFile(File file) {
    System.out.println("Running " + file.getName());
    
    currentSuite = new TestSuiteResult();
    currentSuite.setName(file.getName());
    currentSuiteStarted = System.currentTimeMillis();
  }
  
  public void fileComplete() {
    currentSuite.setTime( (System.currentTimeMillis() - currentSuiteStarted) / 1000d );
  
    currentSuite.writeXml(reportDir);
    
    System.out.println(String.format("Tests run: %d, Failures: %d, Errors: %d, Skipped: %d, Time elapsed: %.3f %s",
        currentSuite.getResults().size(),
        currentSuite.countOutcomes(TestOutcome.FAILURE),
        currentSuite.countOutcomes(TestOutcome.ERROR),
        currentSuite.countOutcomes(TestOutcome.SKIPPED),
        currentSuite.getTime(),
        currentSuite.countOutcomes(TestOutcome.FAILURE) > 0 ? " << FAILURE!" : ""));
  }
  
  public void startFunction(String name) {
    currentCase = new TestCaseResult();
    currentCase.setClassName(currentSuite.getName());
    currentCase.setName(name);
    currentSuite.addCase(currentCase);
    currentCaseStarted = System.currentTimeMillis();
  }
  
  public void functionSucceeded() {
    currentCase.setOutcome(TestOutcome.SUCCESS);
    functionComplete();
  }
  
  public void functionThrew(Exception e) {
    currentCase.setOutcome(TestOutcome.ERROR);
    currentCase.setException(e);
    functionComplete();
  }

  private void functionComplete() {
    currentSuite.setTime( (System.currentTimeMillis() - currentCaseStarted) / 1000d );
  }
  
}
