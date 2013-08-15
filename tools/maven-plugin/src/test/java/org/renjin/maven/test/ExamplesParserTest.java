package org.renjin.maven.test;


import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class ExamplesParserTest extends TestCase {

  public void testInfiniteLoop() throws Exception {

    File testFile = new File(getClass().getResource("/p.hboxp.Rd").getFile());
    String examples = ExamplesParser.parseExamples(testFile);

    TestRunner runner = new TestRunner("foo", new File("targets/test-test-results"), Lists.<String>newArrayList());
    runner.executeFile(testFile, examples);

  }
}
