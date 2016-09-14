package org.renjin.maven.test;

import org.renjin.repackaged.guava.base.Throwables;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repl.JlineRepl;
import org.renjin.sexp.Closure;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Main class running in a forked VM that takes commands from the maven VM and 
 * tries to execute tests.
 */
public class TestExecutor {

  private static final int MAX_DEFAULT_BYTES = 50 * 1024;

  public static final String NAMESPACE_UNDER_TEST = "NAMESPACE_UNDER_TEST";
  public static final String TEST_REPORT_DIR = "TEST_REPORT_DIR";
  public static final String DEFAULT_PACKAGES = "DEFAULT_PACKAGES";
  public static final String OUTPUT_LIMIT = "OUTPUT_LIMIT";


  public static final String MESSAGE_PREFIX = "!!@@@@####";
  public static final String PASS_MESSAGE = "PASS";
  public static final String FAIL_MESSAGE = "FAIL";
  public static final String DONE_MESSAGE = "DONE";
  public static final String START_MESSAGE = "START";


  private String namespaceUnderTest;
  private File testReportDirectory;
  private List<String> defaultPackages;
  private int maxOutputBytes;


  public static void main(String[] args) throws IOException {

    TestExecutor executor = new TestExecutor();
    executor.execute();
  }

  public TestExecutor() {
    namespaceUnderTest = System.getenv(NAMESPACE_UNDER_TEST);
    testReportDirectory = new File(System.getenv(TEST_REPORT_DIR));
    if(Strings.isNullOrEmpty(System.getenv(DEFAULT_PACKAGES))) {
      defaultPackages = Collections.emptyList();
    } else {
      defaultPackages = Arrays.asList(System.getenv(DEFAULT_PACKAGES).split(","));
      for (String defaultPackage : defaultPackages) {
        debug("Default package: " + defaultPackage);
      }
    }
    
    if(!Strings.isNullOrEmpty(System.getenv(DEFAULT_PACKAGES))) {
      maxOutputBytes = Integer.parseInt(System.getenv(OUTPUT_LIMIT));
    } else {
      maxOutputBytes = MAX_DEFAULT_BYTES;
    }
  }

  public TestExecutor(String namespaceUnderTest, List<String> defaultPackages, File testReportDirectory) {
    this.namespaceUnderTest = namespaceUnderTest;
    this.testReportDirectory = testReportDirectory;
    this.defaultPackages = defaultPackages;
  }

  public void execute() throws IOException {

    debug("Starting execution...");

    DataInputStream inputStream = new DataInputStream(System.in);

    try {
      while (true) {
        debug("Waiting for command...");
        String testFilePath = inputStream.readUTF();

        debug("Received command: " + testFilePath);

        executeTest(new File(testFilePath));
      }
    } catch (EOFException e) {
      debug("EOF Caught, exiting.");
    }
  }

  private void debug(final String message) {
    if(ForkedTestController.DEBUG_FORKING) {
      System.err.println("[EXECUTOR] " + message);
    }
  }

  private void sendMessage(String message, String... arguments) {
    System.out.println(MESSAGE_PREFIX + message + MESSAGE_PREFIX + Joiner.on(MESSAGE_PREFIX).join(arguments));
    System.out.flush();
  }

  @VisibleForTesting
  void executeTest(File testFile) throws IOException {
    
    if (testFile.getName().toLowerCase().endsWith(".rd")) {
      executeTestFile(testFile, ExamplesParser.parseExamples(testFile));
    } else {
      executeTestFile(testFile, Files.toString(testFile, Charsets.UTF_8));
    }

    sendMessage(DONE_MESSAGE);
  }

  private PrintStream openTestOutput(File testFile) {
    try {
      return new PrintStream(
          new CappedOutputStream(maxOutputBytes, new FileOutputStream(
              new File(testReportDirectory, TestReporter.suiteName(testFile) + "-output.txt"))));
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void loadLibrary(Session session, String namespaceName, PrintStream testOutput) {
    try {
      session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get(namespaceName)));
    } catch(Exception e) {
      testOutput.println("Failed to load namespace " + namespaceName);
      testOutput.println(Throwables.getStackTraceAsString(e));
      throw new EvalException("Failed to load namespace " + namespaceName, e);
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

  @VisibleForTesting
  void executeTestFile(File sourceFile, String sourceText) throws IOException {


    PrintStream testOutput = openTestOutput(sourceFile);
    try {

      if (isEmpty(sourceText)) {
        // skip empty files or Rd docs with no examples
        return;
      }

      sendMessage(START_MESSAGE, TestCaseResult.ROOT_TEST_CASE);

      Session session = createSession(testOutput, sourceFile.getParentFile());

      // Examples assume that the package is already on the search path
      if (sourceFile.getName().endsWith(".Rd")) {
        loadLibrary(session, namespaceUnderTest, testOutput);
      }

      UnsupportedTerminal term = new UnsupportedTerminal();
      InputStream in = new ByteArrayInputStream(sourceText.getBytes(Charsets.UTF_8));
      PrintStream outputStream = new PrintStream(testOutput);
      ConsoleReader consoleReader = new ConsoleReader(in, outputStream, term);
      JlineRepl repl = new JlineRepl(session, consoleReader);
      repl.setErrorStream(outputStream);
      repl.setInteractive(false);
      repl.setEcho(true);
      repl.setStopOnError(true);

      try {
        repl.run();
        sendMessage(PASS_MESSAGE);

        // Note that we only catch Exceptions here --
        // Errors should NOT be caught as we should allow the JVM to cleanup and shutdown
        // a new JVM will be started for the subsequent test.
      } catch (Exception e) {
        e.printStackTrace(testOutput);
        sendMessage(FAIL_MESSAGE);
        return;
      }

      // look for "junit-style" test functions.
      // This is renjin's own convention, but it's nice to be
      // able to see the results of many tests rather than 
      // topping at the first error
      testOutput.println();
      for (Symbol name : session.getGlobalEnvironment().getSymbolNames()) {
        if (name.getPrintName().startsWith("test.")) {
          SEXP value = session.getGlobalEnvironment().getVariable(name);
          if (isZeroArgFunction(value)) {
            executeTestFunction(session.getTopLevelContext(), name, testOutput);
          }
        }
      }
    } finally {
      testOutput.close();
    }
  }


  private boolean isEmpty(String sourceText) {
    for(int i=0;i!=sourceText.length();++i) {
      if(!Character.isWhitespace(sourceText.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private void executeTestFunction(Context context, Symbol name, PrintStream testOutput) {
    try {
      testOutput.print("Executing " + name.getPrintName() + "... ");
      sendMessage(START_MESSAGE, name.getPrintName());
      context.evaluate(FunctionCall.newCall(name));
      sendMessage(PASS_MESSAGE);
      testOutput.println("PASSED");
      
    } catch (EvalException e) {
      // Pretty-print R stack trace ONLY
      testOutput.println("FAILED");
      testOutput.println("ERROR: " + e.getMessage());
      e.printRStackTrace(testOutput);
      sendMessage(FAIL_MESSAGE);

    } catch(Error e) {
      // Oops, we crashed the VM...
      testOutput.println("FAILED");
      e.printStackTrace(testOutput);
      sendMessage(FAIL_MESSAGE);

      // Abort...
      throw e;

    } catch (Throwable e) {
      // Uncaught exception: print Java stack trace
      testOutput.println("FAILED");
      testOutput.println("UNCAUGHT EXCEPTION: " + e.getMessage());
      e.printStackTrace(testOutput);
      sendMessage(FAIL_MESSAGE);
    }
  }


  private Session createSession(PrintStream testOutput, File workingDir) throws IOException {

    PrintWriter testOutputWriter = new PrintWriter(testOutput);

    Session session = SessionBuilder.buildDefault();
    session.setWorkingDirectory(
        session.getFileSystemManager()
            .resolveFile(workingDir.toURI().toString()));

    session.setStdErr(testOutputWriter);
    session.setStdOut(testOutputWriter);

    if(defaultPackages.isEmpty()) {
      System.err.println("No default packages specified");
    }

    for(String pkg : defaultPackages) {
      System.err.println("Loading default package " + pkg);
      loadLibrary(session, pkg, testOutput);
    }

    return session;
  }
}
