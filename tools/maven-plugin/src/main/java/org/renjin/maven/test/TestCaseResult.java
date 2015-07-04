package org.renjin.maven.test;

public class TestCaseResult {

  public static final String ROOT_TEST_CASE = "(root)";
  
  /**
   * Time in seconds
   */
  private double time;
    
  private String className;
  
  private String name;
  
  private String errorMessage;
  
  private TestOutcome outcome;

  public boolean isRootScript() {
    return name.equals(ROOT_TEST_CASE);
  }
  
  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public TestOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(TestOutcome outcome) {
    this.outcome = outcome;
  }
  
}
