package org.renjin.maven.test;


import junit.framework.TestCase;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;

public class ExamplesParserTest extends TestCase {

  public void testInfiniteLoop() throws Exception {

    File reportDir = Files.createTempDir();
    File testFile = new File(getClass().getResource("/man/p.hboxp.Rd").getFile());
    String examples = ExamplesParser.parseExamples(testFile);
    
    TestExecutor runner = new TestExecutor("foo",  Lists.<String>newArrayList(), reportDir);
    runner.executeTestFile(testFile, examples);

  }
  
  public void testDontRun() throws IOException {
    File testFile = new File(getClass().getResource("/man/proto.Rd").getFile());
    String examples = ExamplesParser.parseExamples(testFile);
    
    assertFalse(examples.contains("# DO NOT RUN FOR THE LOVE OF "));

  }
}
