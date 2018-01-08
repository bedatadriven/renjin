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
    
    TestExecutor runner = new TestExecutor("base",  Lists.<String>newArrayList(), reportDir);
    runner.executeTestFile(testFile, examples);

  }
  
  public void testDontRun() throws IOException {
    File testFile = new File(getClass().getResource("/man/proto.Rd").getFile());
    String examples = ExamplesParser.parseExamples(testFile);
    
    assertFalse(examples.contains("# DO NOT RUN FOR THE LOVE OF "));

  }

  public void testRdComments() throws IOException {
    File testFile = new File(getClass().getResource("/man/generate.Guilds.Rd").getFile());
    String examples = ExamplesParser.parseExamples(testFile);

    assertEquals("\ngenerate.Guilds(theta=200,alpha_x = 0.005, alpha_y = 0.001,J=20000);\n", examples);
  }
}
