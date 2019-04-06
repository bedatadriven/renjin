/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */
package org.renjin.packaging.test;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.packaging.CorePackageBuilder;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.base.Throwables;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repl.JlineRepl;
import org.renjin.sexp.*;

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

  private String namespaceUnderTest;
  private File testReportDirectory;
  private List<String> defaultPackages;
  private int maxOutputBytes = Integer.MAX_VALUE;

  private TestListener listener;

  public TestExecutor(TestListener listener) {
    this.listener = listener;
    namespaceUnderTest = System.getenv(NAMESPACE_UNDER_TEST);
    if(Strings.isNullOrEmpty(namespaceUnderTest)) {
      namespaceUnderTest = CorePackageBuilder.packageNameFromWorkingDirectory();
    }
    testReportDirectory = new File(System.getenv(TEST_REPORT_DIR));
    if(Strings.isNullOrEmpty(System.getenv(DEFAULT_PACKAGES))) {
      defaultPackages = Collections.emptyList();
    } else {
      defaultPackages = Arrays.asList(System.getenv(DEFAULT_PACKAGES).split(","));
      for (String defaultPackage : defaultPackages) {
        listener.debug("Default package: " + defaultPackage);
      }
    }
    
    if(!Strings.isNullOrEmpty(System.getenv(OUTPUT_LIMIT))) {
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


  @VisibleForTesting
  void executeTest(File testFile) throws IOException {
    
    if (testFile.getName().toLowerCase().endsWith(".rd")) {
      executeTestFile(testFile, ExamplesParser.parseExamples(testFile));
    } else {
      executeTestFile(testFile, Files.toString(testFile, Charsets.UTF_8));
    }

    listener.done();
  }

  public void executeTestDir(File dir) throws IOException {
    File[] files = dir.listFiles();
    if(files != null) {
      for (File file : files) {
        if(file.getName().toLowerCase().endsWith(".r") ||
           file.getName().toLowerCase().endsWith(".rd")) {
          executeTest(file);
        }
      }
    }
  }

  private PrintStream openTestOutput(File testFile) {

    if(!testReportDirectory.exists()) {
      testReportDirectory.mkdirs();
    }

    try {
      return new PrintStream(
          new CappedOutputStream(maxOutputBytes, new FileOutputStream(
              new File(testReportDirectory, TestReporter.suiteName(testFile) + "-output.txt"))));
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private File createPlotDirectory(File testFile) {
    File directory = new File(testReportDirectory, TestReporter.suiteName(testFile));
    if(!directory.exists()) {
      boolean created = directory.mkdirs();
      if(!created) {
        throw new EvalException("Could not create directory '" + directory.getAbsolutePath() + "'");
      }
    }
    return directory;
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

      listener.start(TestCaseResult.ROOT_TEST_CASE);

      Session session = createSession(testOutput, sourceFile.getParentFile());
      session.getOptions().set("device", graphicsDevice(session, sourceFile));

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
        listener.pass();

      } catch (Throwable e) {
        e.printStackTrace(testOutput);
        listener.fail();

        if(e instanceof OutOfMemoryError) {
          throw (OutOfMemoryError)e;
        }
        return;
      }

      // look for "junit-style" test functions.
      // This is renjin's own convention, but it's nice to be
      // able to see the results of many tests rather than 
      // topping at the first error
      testOutput.println();
      for (Symbol name : session.getGlobalEnvironment().getSymbolNames()) {
        if (name.getPrintName().startsWith("test.")) {
          SEXP value = session.getGlobalEnvironment().getVariable(session.getTopLevelContext(), name);
          if (isZeroArgFunction(value)) {
            executeTestFunction(session.getTopLevelContext(), name, testOutput);
          }
        }
      }

      // Cleanup graphics, etc.
      repl.close();

    } finally {
      testOutput.close();
    }
  }

  private SEXP graphicsDevice(Session session, File sourceFile) {

    PairList.Builder arguments = new PairList.Builder();
    arguments.add("filename", StringArrayVector.valueOf(createPlotDirectory(sourceFile).getAbsolutePath() + File.separator + "Rplot%03d.svg"));

    return new Closure(session.getGlobalEnvironment(), Null.INSTANCE,
        new FunctionCall(Symbol.get("svg"), arguments.build()));
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
      listener.start(name.getPrintName());
      context.evaluate(FunctionCall.newCall(name));
      listener.pass();
      testOutput.println("PASSED");
      
    } catch (EvalException e) {
      // Pretty-print R stack trace ONLY
      testOutput.println("FAILED");
      testOutput.println("ERROR: " + e.getMessage());
      e.printRStackTrace(testOutput);
      listener.fail();

    } catch(Error e) {
      // Oops, we crashed the VM...
      testOutput.println("FAILED");
      e.printStackTrace(testOutput);
      listener.fail();

      // Abort...
      throw e;

    } catch (Throwable e) {
      // Uncaught exception: print Java stack trace
      testOutput.println("FAILED");
      testOutput.println("UNCAUGHT EXCEPTION: " + e.getMessage());
      e.printStackTrace(testOutput);
      listener.fail();
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

    // Setup options for testthat so that test results are written in junit format
    // to the expected location
    StringVector testOut = StringVector.valueOf(
        new File(testReportDirectory, "TEST-testthat-results.xml").getAbsolutePath());

    PairList.Builder options = new PairList.Builder();
    options.add("testthat.default_check_reporter", StringVector.valueOf("junit"));
    options.add("testthat.junit.output_file", testOut);
    options.add("testthat.output_file", testOut);

    session.getTopLevelContext().evaluate(new FunctionCall(Symbol.get("options"), options.build()));

    return session;
  }

}
