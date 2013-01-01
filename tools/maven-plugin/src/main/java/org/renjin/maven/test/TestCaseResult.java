package org.renjin.maven.test;

public class TestCaseResult {
    
  /**
   * Time in seconds
   */
  private double time;
    
  private String className;
  
  private String name;
  
  private Exception exception;
  
  private TestOutcome outcome;

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

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public TestOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(TestOutcome outcome) {
    this.outcome = outcome;
  }
  
}
