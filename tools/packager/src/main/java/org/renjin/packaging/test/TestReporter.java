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

import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import static java.lang.String.format;

public class TestReporter {



  private List<TestSuiteResult> suites = Lists.newArrayList();
  private TestSuiteResult currentSuite;
  private long currentSuiteStarted;
  private File reportsDir;

  private TestCaseResult currentCase;
  private long currentCaseStarted;

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
    currentSuite.setClassName(suiteName(file));
    currentSuiteStarted = System.currentTimeMillis();
    suites.add(currentSuite);
  }

  public static String suiteName(File file) {
    String name = file.getName();
    if(name.endsWith(".R")) {
      return name.substring(0, name.length() - ".R".length());
    } else if(name.endsWith(".Rd")) {
      return name.substring(0, name.length() - ".Rd".length()) + "-examples";
    } else {
      return name;
    }
  }


  public void fileComplete() {
    currentSuite.setTime( (System.currentTimeMillis() - currentSuiteStarted) / 1000d );
    currentSuite.writeXml(reportsDir);

    printResultsBanner(System.out);

  }


  private void printResultsBanner(PrintStream out) {
    out.printf("Tests run: %d, Failures: %d, Errors: %d, Skipped: %d, Time elapsed: %.3f %s%n",
        currentSuite.getResults().size(),
        currentSuite.countOutcomes(TestOutcome.FAILURE),
        currentSuite.countOutcomes(TestOutcome.ERROR),
        currentSuite.countOutcomes(TestOutcome.SKIPPED),
        currentSuite.getTime(),
        currentSuite.hasFailures() ? " << FAILURE!" : "");
  }

  public void testCaseStarting(String name) {
    if(name.startsWith("test.")) {
      name = name.substring("test.".length());
    }

    currentCase = new TestCaseResult();
    currentCase.setClassName(currentSuite.getClassName());
    currentCase.setName(name);
    currentSuite.addCase(currentCase);
    currentCaseStarted = System.currentTimeMillis();
  }


  public void timeout(long timeoutLengthMs) {
    if (currentCase == null) {
      System.err.println("currentCase is null, testCaseStarting was not called before timeout");
      testCaseStarting("unknown");
    }

    currentCase.setOutcome(TestOutcome.ERROR);
    currentCase.setErrorMessage("Timed out after " + timeoutLengthMs + " ms");
    if (currentCase.isRootScript()) {
      System.err.printf("Evaluation of %s timed out%n", currentSuite.getScriptFile().getName());
    } else {
      System.err.printf("%s() in %s timed out%n",
          currentCase.getName(),
          currentSuite.getScriptFile().getName());
    }
    functionComplete();
  }

  public void testCaseSucceeded() {
    currentCase.setOutcome(TestOutcome.SUCCESS);
    functionComplete();
  }
  
  public void testCaseFailed() {
    testCaseFailed(null);
  }

  public void testCaseFailed(String message)  {
    currentCase.setErrorMessage(message);
    currentCase.setOutcome(TestOutcome.ERROR);
    if(currentCase.isRootScript()) {
      System.err.printf("Evaluation of %s failed%n",
          currentSuite.getScriptFile().getName());
    } else {
      System.err.printf("%s() in %s failed%n",
          currentCase.getName(),
          currentSuite.getScriptFile().getName());
    }
    functionComplete();
  }

  private void functionComplete() {
    currentCase.setTime( (System.currentTimeMillis() - currentCaseStarted) / 1000d );
  }

  public boolean allTestsSucceeded() {
    for(TestSuiteResult suite : suites) {
      if(suite.hasFailures()) {
        return false;
      }
    }
    return true;
  }

  public TestSuiteResult getCurrentSuite() {
    return currentSuite;
  }

  public void testCaseInterrupted() {
    System.err.println(format("Interrupted while waiting for %s() in %s to complete",
        currentCase.getName(),
        currentSuite.getScriptFile().getName()));

  }
}
