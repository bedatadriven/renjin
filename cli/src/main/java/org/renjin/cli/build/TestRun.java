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
package org.renjin.cli.build;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import org.apache.commons.vfs2.FileObject;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.packaging.BuildException;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.repl.JlineRepl;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Runs package tests
 */
public class TestRun {

  private File stagingDir;
  private File testDir;

  private int count;
  private int passedCount;

  public TestRun(File stagingDir, File testDir) {
    this.stagingDir = stagingDir;
    this.testDir = testDir;
  }

  public void execute() {
    for (File testFile : findTestSources(testDir)) {
      count++;
      System.out.print("Running test " + testFile.getName() + "... ");
      System.out.flush();
      boolean passed = executeTest(testFile);
      if(passed) {
        System.out.println("OK");
        passedCount++;
      } else {
        System.out.println("ERROR");
      }
    }

    System.out.println();
    System.out.println(String.format("TEST RESULTS: %d/%d passed.", passedCount, count));
  }

  private List<File> findTestSources(File dir) {
    List<File> testFiles = new ArrayList<File>();
    File[] files = testDir.listFiles();
    if(files != null) {
      for (File file : files) {
        if (file.getName().toUpperCase().endsWith(".R")) {
          testFiles.add(file);
        }
      }
    }
    Collections.sort(testFiles, new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
      }
    });
    return testFiles;
  }

  private Session newSession() {
    SessionBuilder builder = new SessionBuilder().withDefaultPackages();
    builder.setPackageLoader(createPackageLoader());
    Session session = builder.build();
    session.setWorkingDirectory(resolveTestDir(session));
    return session;
  }

  private FileObject resolveTestDir(Session session) {
    try {
      return session.getFileSystemManager().resolveFile(testDir, ".");
    } catch (Exception e) {
      throw new BuildException("Exception resolving test dir path", e);
    }
  }

  private ClasspathPackageLoader createPackageLoader() {
    try {
      URL packageUnderTestUrl = stagingDir.toURI().toURL();
      URLClassLoader classLoader = new URLClassLoader(new URL[]{packageUnderTestUrl}, getClass().getClassLoader());
      return new ClasspathPackageLoader(classLoader);
    } catch (Exception e) {
      throw new BuildException("Exception creating package loader", e);
    }
  }

  private boolean executeTest(File testFile)  {

    File outputFile = new File(testFile.getParentFile(), testFile.getName() + "out");
    try(PrintStream output = new PrintStream(outputFile)) {
      try {
        Session session = newSession();
        session.setStdOut(new PrintWriter(output));
        session.setStdErr(new PrintWriter(output));

        UnsupportedTerminal term = new UnsupportedTerminal();
        InputStream in = new FileInputStream(testFile);
        ConsoleReader consoleReader = new ConsoleReader(in, output, term);
        JlineRepl repl = new JlineRepl(session, consoleReader);
        repl.setInteractive(false);
        repl.setEcho(true);
        repl.setStopOnError(true);

        repl.run();
        return true;
      } catch (Exception e) {
        e.printStackTrace(output);
        return false;
      }
    } catch (FileNotFoundException e) {
      throw new BuildException("Couldn't create test output file " + outputFile.getAbsolutePath());
    }
  }
}
