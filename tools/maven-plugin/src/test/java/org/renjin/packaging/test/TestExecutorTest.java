/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.packaging.test;

import org.junit.Before;
import org.junit.Test;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestExecutorTest {

  private List<String> defaultPackages = Collections.emptyList();
  private File reportDir;

  @Before
  public void setUp() throws Exception {
    reportDir = Files.createTempDir();
  }

  @Test
  public void testThatExceptionsInTestShouldNotEscape() throws Exception {
    TestExecutor runner = new TestExecutor("base", defaultPackages, new SimpleListener(), reportDir);
    File testFile = new File(Resources.getResource("man/mean.Rd").getFile());
    runner.executeTest(testFile);
  }

  @Test
  public void testsShouldTimeout() throws IOException, InterruptedException {

    SimpleListener listener = new SimpleListener();

    TestExecutor runner = new TestExecutor("base", defaultPackages, listener, reportDir);
    runner.setTimeoutMillis(TimeUnit.SECONDS.toMillis(1));

    File testFile = new File(Resources.getResource("man/mean.Rd").getFile());
    String testScript = "Sys.sleep(30)\n";
    runner.executeTestFileWithTimeout(testFile, testScript);

    assertThat(listener.getFailCount(), equalTo(1));
  }

}
