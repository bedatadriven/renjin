package org.renjin.cli.build;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import org.apache.commons.vfs2.FileObject;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.eval.SessionController;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
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
    builder.bind(PackageLoader.class, createPackageLoader());
//    builder.bind(SessionController.class, createSessionControler)
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
    PrintStream output;
    try {
      output = new PrintStream(outputFile);
    } catch (FileNotFoundException e) {
      throw new BuildException("Couldn't create test output file " + outputFile.getAbsolutePath());
    }

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
  }
  
  private class TestSessionController extends SessionController {
    @Override
    public boolean isInteractive() {
      return false;
    }
  }
}
