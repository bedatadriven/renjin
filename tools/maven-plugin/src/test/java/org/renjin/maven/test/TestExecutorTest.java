package org.renjin.maven.test;

import junit.framework.TestCase;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class TestExecutorTest extends TestCase {

  public void testThatExceptionsInTestShouldNotEscape() throws Exception {
    File reportDir = Files.createTempDir();
    List<String> defaultPackages = Collections.<String>emptyList();
    TestExecutor runner = new TestExecutor("base", defaultPackages, reportDir);
    File testFile = new File(Resources.getResource("man/mean.Rd").getFile());
    runner.executeTest(testFile);
  }
}
