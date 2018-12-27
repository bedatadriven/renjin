/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.maven.test;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Stopwatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Forks and controls a external JVM in which tests are actually run.
 *
 * <p>This allows us to handle and report failures on tests which timeout or manage to thoroughly 
 * crash the JVM.</p>
 */
public class ForkedTestController {

  public static final boolean DEBUG_FORKING = false;
  private final Log log;

  private long timeoutMillis = TimeUnit.MINUTES.toMillis(3);

  private Map<String, String> environmentVariables = new HashMap<>();

  private File testReportsDirectory;
  private TestReporter reporter;
  private String argLine;

  private Fork fork;

  public ForkedTestController() {
    this(new SystemStreamLog());
  }

  public ForkedTestController(Log log) {
    this.log = log;
  }

  public void setNamespaceUnderTest(String namespace) {
    this.environmentVariables.put(TestExecutor.NAMESPACE_UNDER_TEST, namespace);
  }

  /**
   * @param outputLimit the limit to place on test output, in bytes
   */
  public void setOutputLimit(int outputLimit) {
    this.environmentVariables.put(TestExecutor.OUTPUT_LIMIT, Integer.toString(outputLimit));
  }
  
  public void setClassPath(String classPath) {
    this.environmentVariables.put("CLASSPATH", classPath);
  }

  public void setTestReportDirectory(File testReportDirectory) {
    this.testReportsDirectory = testReportDirectory;
    this.environmentVariables.put(TestExecutor.TEST_REPORT_DIR, testReportDirectory.getAbsolutePath());
  }

  public void setDefaultPackages(List<String> packages) {
    if(packages != null) {
      this.environmentVariables.put(TestExecutor.DEFAULT_PACKAGES, Joiner.on(",").join(packages));
    }
  }

  public void setTimeout(long timeout, TimeUnit timeUnit) {
    timeoutMillis = timeUnit.toMillis(timeout);
  }

  public void executeTests(File testSourceDirectory) throws MojoExecutionException {

    log.info("Running tests in " + testSourceDirectory.getAbsolutePath());

    if(testSourceDirectory.isDirectory()) {
      File[] testFiles = testSourceDirectory.listFiles();
      if(testFiles != null) {
        for (File testFile : testFiles) {
          String testFileName = testFile.getName().toUpperCase();
          if(testFileName.endsWith(".R") || testFileName.endsWith(".RD")) {
            executeTest(testFile);
          }
        }
      }
    }
  }

  public void executeTest(File testFile) throws MojoExecutionException {

    if(reporter == null) {
      reporter = new TestReporter(testReportsDirectory);
      reporter.start();
    }

    if(fork == null) {
      startFork();
    }

    reporter.startFile(testFile);

    try {
      // Send the command to run the test
      fork.sendCommand(testFile.getAbsolutePath());

    } catch (IOException e) {
      log.error("Failed to send command to fork", e);
      shutdown();
    }

    if(!pollResults()) {
      // If the test file timed out, or the forked JVM otherwise
      // barfed, then clean up our fork so that the next test file starts
      // with a fresh JVM.
      destroyFork();
    }

    reporter.fileComplete();
  }

  /**
   *
   * @return true if the test run completed in the allotted time with the JVM in the ready state.
   */
  private boolean pollResults() {

    // Impose a total timeout on this test file
    Stopwatch stopwatch = Stopwatch.createStarted();

    // Listen for test results
    try {
      while (true) {
        ForkMessage message = fork.readMessage(500, TimeUnit.MILLISECONDS);
        if (message != null) {
          switch (message.getType()) {
            case TestExecutor.START_MESSAGE:
              reporter.testCaseStarting(message.getArgument());
              break;

            case TestExecutor.FAIL_MESSAGE:
              reporter.testCaseFailed();
              break;

            case TestExecutor.PASS_MESSAGE:
              reporter.testCaseSucceeded();
              break;

            case TestExecutor.DONE_MESSAGE:
              return true;

            default:
              log.error("Unknown message: " + message.getType());
              break;
          }
        }
        if(timeoutMillis > 0 && stopwatch.elapsed(TimeUnit.MILLISECONDS) > timeoutMillis) {
          reporter.timeout(timeoutMillis);
          return false;
        }
      }
    } catch (InterruptedException e) {
      log.debug("ForkedTestController interrupted.");
      Thread.currentThread().interrupt();
      reporter.testCaseInterrupted();
      return false;

    } catch (Exception e) {
      log.debug("ForkedTestController received exception while waiting for results.", e);
      reporter.testCaseFailed(e.getMessage());
      return false;
    }
  }

  private void startFork() throws MojoExecutionException {

    try {

      List<String> command = new ArrayList<>();
      command.add("java");
      if(argLine != null) {
        command.add(argLine);
      }
      command.add(TestExecutor.class.getName());

      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command(command);
      processBuilder.environment().putAll(environmentVariables);
      processBuilder.redirectErrorStream(true);

      this.fork = new Fork(log, processBuilder.start());

    } catch (Exception e) {
      throw new MojoExecutionException("Could not start forked JVM", e);
    }
  }

  public void shutdown() {
    if(fork != null) {
      destroyFork();
    }
  }

  private void destroyFork() {
    fork.shutdown();
    fork = null;
  }

  public boolean allTestsSucceeded() {
    return reporter == null || reporter.allTestsSucceeded();
  }

  public void setArgLine(String argLine) {
    this.argLine = argLine;
  }

}
