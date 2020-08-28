package org.renjin.packaging.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

class SimpleListener implements TestListener {

  private String testFile = "";
  private String executingTestName;
  private int passCount;
  private int failCount;
  private boolean countTimoutAsError;

  private final List<String> failedCases = new ArrayList<>();
  private final List<String> passCases = new ArrayList<>();

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

  public SimpleListener() {
    this(false);
  }

  public SimpleListener(boolean countTimoutAsError) {
    this.countTimoutAsError = countTimoutAsError;
  }

  @Override
  public void pass() {
    passCount++;
    passCases.add(testFile + " " + executingTestName);
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
    if (countTimoutAsError) {
      failCount++;
      failedCases.add(testFile + " " + executingTestName);
    }
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

  public void saveResults(File reportDir) {
    // ReportDir is required and always created by the TestExecutor so no need to check for null and non existing dirs
    File resultFile = new File(reportDir,"renjin-test-results.log");
    try(Writer writer = new FileWriter(resultFile)) {
      for (String passCase : passCases) {
        writer.write( passCase + " pass\n");
      }
      for (String failCase : failedCases) {
        writer.write(failCase + " fail\n");
      }
      writer.flush();
    } catch (IOException e) {
      System.err.println("Failed to save test results to file " + resultFile.getAbsolutePath());
      e.printStackTrace(System.err);
    }
  }
}
