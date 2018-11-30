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


import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.ClasspathPackageLoader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class BuilderTest {

  @Test
  public void simple() throws Exception {

    File sourceRoot = findTestPackageRoot("square");

    Builder.execute("build", sourceRoot.getAbsolutePath());

    Session session = sessionWithTestPackage("square");
    session.getTopLevelContext().evaluate(RParser.parseSource("library('org.renjin.cran:square')"));
    session.getTopLevelContext().evaluate(RParser.parseSource("stopifnot(sqr(2) == 4)"));
  }

  @Test
  public void withDependencies() throws Exception {
    File sourceRoot = findTestPackageRoot("pkgwithdeps");

    Builder.execute("build", sourceRoot.getAbsolutePath() );
  }

  /**
   * Finds the source root directory for a test package that lives within src/test/resources
   */
  private File findTestPackageRoot(final String name) {
    return new File(getClass().getResource("/" + name + "/DESCRIPTION").getFile()).getParentFile();
  }

  private Session sessionWithTestPackage(final String name) {
    URL jarUrl = getClass().getResource("/" + name + "/build/" + name + "-0.1.jar");

    URLClassLoader classLoader = new URLClassLoader(new URL[] {jarUrl}, getClass().getClassLoader());
    ClasspathPackageLoader packageLoader = new ClasspathPackageLoader(classLoader);

    return new SessionBuilder().setPackageLoader(packageLoader).withDefaultPackages().build();
  }
}