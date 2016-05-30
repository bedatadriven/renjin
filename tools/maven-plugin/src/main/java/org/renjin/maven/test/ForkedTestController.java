package org.renjin.maven.test;

import org.apache.maven.plugin.MojoExecutionException;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;

import java.io.*;
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
  
  private long timeoutMillis = TimeUnit.MINUTES.toMillis(3);

  private Map<String, String> environmentVariables = new HashMap<String, String>();

  private Process process;
  private DataOutputStream processChannel;
  private File testReportsDirectory;
  private TestReporter reporter;

  public void setNamespaceUnderTest(String namespace) {
    this.environmentVariables.put(TestExecutor.NAMESPACE_UNDER_TEST, namespace);
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

    System.out.println("Running tests in " + testSourceDirectory.getAbsolutePath());

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

    if(process == null) {
      startFork();
    }

    reporter.startFile(testFile);

    try {
      // Send the command to run the test
      processChannel.writeUTF(testFile.getAbsolutePath());
      processChannel.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Listen for test results

    ResultListener listener = new ResultListener(process.getInputStream());
    Thread listeningThread = new Thread(listener);
    listeningThread.start();
    try {
      listeningThread.join(timeoutMillis);
    } catch (InterruptedException e) {
      reporter.testCaseInterrupted();
      reporter.fileComplete();
      return;
    }
    if(listeningThread.isAlive()) {
      // if we didn't succeed in joining, then it means we have timed out.
      reporter.timeout(timeoutMillis);
      destroyFork();
    }
    reporter.fileComplete();
  }

  private void startFork() throws MojoExecutionException {

    try {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("java", TestExecutor.class.getName());
      processBuilder.environment().putAll(environmentVariables);
      processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
      process = processBuilder.start();
      processChannel = new DataOutputStream(process.getOutputStream());
    } catch (Exception e) {
      throw new MojoExecutionException("Could not start forked JVM", e);
    }
  }

  public void shutdown() {
    if(process != null) {
      try {
        processChannel.close();
        process.destroy();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        process = null;
        processChannel = null;
      }
    }
  }

  private void destroyFork() {
    try {
      process.destroy();
    } catch (Exception e) {
      e.printStackTrace();
    }
    process = null;
    processChannel = null;
  }

  public boolean allTestsSucceeded() {
    return reporter == null || reporter.allTestsSucceeded();
  }
  
  public class ResultListener implements Runnable {

    private final BufferedReader reader;

    private boolean processRunning = true;

    public ResultListener(InputStream in) {
      this.reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
    }

    @Override
    public void run() {
      while(true) {
        String line;
        try {
          line = reader.readLine();
        } catch (IOException e) {
          // Not sure under what situation this could happen but consider it a test failure
          System.err.println("Error reading from forked test executor: " + e.getMessage());
          e.printStackTrace();
          reporter.testCaseFailed();
          destroyFork();
          break;
        }
        if(line == null) {
          // Process exited!!
          reporter.testCaseFailed();
          try {
            System.err.println("Forked JVM exited with code " + process.waitFor());
          } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for process to exit.");
          }
          destroyFork();
          break;

        } else {
          if(DEBUG_FORKING) {
            System.out.println("[CHANNEL] " + line);
          }
          if (line.startsWith(TestExecutor.MESSAGE_PREFIX)) {
            String message[] = line.substring(TestExecutor.MESSAGE_PREFIX.length()).split(TestExecutor.MESSAGE_PREFIX);
            if (message[0].equals(TestExecutor.START_MESSAGE)) {
              reporter.testCaseStarting(message[1]);
            } else if (message[0].equals(TestExecutor.FAIL_MESSAGE)) {
              reporter.testCaseFailed();
            } else if (message[0].equals(TestExecutor.PASS_MESSAGE)) {
              reporter.testCaseSucceeded();
            } else if (message[0].equals(TestExecutor.DONE_MESSAGE)) {
              break;
            }
          }
        }
      }
    }
  }
}
