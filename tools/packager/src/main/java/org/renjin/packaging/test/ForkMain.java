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

import org.renjin.packaging.CorePackageBuilder;
import org.renjin.repackaged.guava.base.Strings;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Main class that is executed by {@link ForkedTestController} and runs tests
 * as instructed by the messages coming in from standard input.
 */
public class ForkMain {

  public static final String NAMESPACE_UNDER_TEST = "NAMESPACE_UNDER_TEST";
  public static final String TEST_REPORT_DIR = "TEST_REPORT_DIR";
  public static final String DEFAULT_PACKAGES = "DEFAULT_PACKAGES";


  public static void main(String[] args) throws IOException {

    ForkReporter listener = new ForkReporter();

    List<String> defaultPackages;
    if(Strings.isNullOrEmpty(System.getenv(DEFAULT_PACKAGES))) {
      defaultPackages = Collections.emptyList();
    } else {
      defaultPackages = Arrays.asList(System.getenv(DEFAULT_PACKAGES).split(","));
      for (String defaultPackage : defaultPackages) {
        listener.debug("Default package: " + defaultPackage);
      }
    }

    String namespaceUnderTest = System.getenv(NAMESPACE_UNDER_TEST);
    if (Strings.isNullOrEmpty(namespaceUnderTest)) {
      namespaceUnderTest = CorePackageBuilder.packageNameFromWorkingDirectory();
    }

    File testReportDirectory = new File(System.getenv(TEST_REPORT_DIR));

    TestExecutor executor = new TestExecutor(namespaceUnderTest, defaultPackages, listener, testReportDirectory);

    listener.debug("Starting execution...");

    DataInputStream inputStream = new DataInputStream(System.in);

    try {
      while (true) {
        listener.debug("Waiting for command...");
        String testFilePath = inputStream.readUTF();

        listener.debug("Received command: " + testFilePath);

        executor.executeTest(new File(testFilePath));
      }
    } catch (EOFException e) {
      listener.debug("EOF Caught, exiting.");
    }
  }
}
