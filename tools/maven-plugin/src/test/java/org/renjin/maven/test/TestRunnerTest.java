package org.renjin.maven.test;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

public class TestRunnerTest extends TestCase {

  public void testExamples() throws Exception {
    TestRunner runner = new TestRunner("base", new File("target/renjin-test-reports"),
        Collections.<String>emptyList());
    runner.run(new File("src/test/resources/man"));
  }
}
