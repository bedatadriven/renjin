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

import io.airlift.airline.*;
import org.renjin.packaging.DefaultPackageList;
import org.renjin.packaging.DefaultPackages;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple main method that invokes tests in the directories provided by
 * environment variables.
 *
 * Exits with 0 if all tests pass.
 */
@Command(name = "test")
public class TestMain {

  static {
    // Silence warnings about not being able to load native libs
    Logger.getLogger("com.github.fommil.netlib").setLevel(Level.SEVERE);
  }


  @Option(name = "--name", description = "The name of the package")
  private String packageName;

  @Option(name = "--report-dir", required = true)
  private File reportDir;

  @Option(name = "--timeout", description = "Timeout for test scripts in seconds")
  private long timeout = 60;

  @Inject
  private DefaultPackageList defaultPackages;

  @Arguments
  private List<String> testDirectories;


  static {
    // Silence warnings about not being able to load native libs
    Logger.getLogger("com.github.fommil.netlib").setLevel(Level.SEVERE);
  }

  public void run() throws IOException, InterruptedException {

    SimpleListener listener = new SimpleListener();

    List<String> defaultPackages;
    if(this.defaultPackages == null) {
      defaultPackages = DefaultPackages.DEFAULT_PACKAGES;
    } else {
      defaultPackages = this.defaultPackages.getList();
    }

    TestExecutor executor = new TestExecutor(packageName, defaultPackages, listener, reportDir);
    executor.setTimeoutMillis(TimeUnit.SECONDS.toMillis(timeout));

    for (String arg : testDirectories) {
      executor.executeTestDir(new File(arg));
    }

    listener.printResults();

    if(listener.getFailCount() > 0) {
      System.exit(-1);
    } else {
      System.exit(0);
    }
  }


  public static void main(String[] args) throws IOException, InterruptedException {

    SingleCommand<TestMain> command =
        SingleCommand.singleCommand(TestMain.class);

    TestMain compiler;
    try {
      compiler = command.parse(args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.err.println();
      Help.help(command.getCommandMetadata());

      System.exit(-1);
      return;
    }

    compiler.run();
  }

}
