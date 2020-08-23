package org.renjin.packaging.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class SimpleListener implements TestListener {

  private String testFile = "";
  private String executingTestName;
  private int passCount;
  private int failCount;

  private List<String> failedCases = new ArrayList<>();

  @Override
  public void debug(String message) {
    System.out.println("[DEBUG] " + message);
  }

  @Override
  public void startFile(File testFile) {
    this.testFile = testFile.getName();
  }

  @Override
  public void start(String testName) {
    this.executingTestName = testName;
  }

  @Override
  public void pass() {
    passCount++;
  }

  @Override
  public void fail() {
    failCount++;
    failedCases.add(testFile + " " + executingTestName);
    System.err.println(testFile + " " + executingTestName + " failed.");
  }

  @Override
  public void done() {
  }

  @Override
  public void timeout() {
    System.err.println(testFile + " " + executingTestName + " timed out.");
  }

  public int getFailCount() {
    return failCount;
  }

  public void printResults() {
    if(!failedCases.isEmpty()) {
      System.err.println("Failed test cases:");
      for (String failedCase : failedCases) {
        System.err.println("  " + failedCase);
      }
      System.err.println();
    }
    System.err.printf("Tests complete: %d/%d passed.%n", passCount, passCount + failCount);
  }
}
