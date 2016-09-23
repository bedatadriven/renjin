/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur;


import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class MassTest {

  @Test
  @Ignore("WIP")
  public void test() throws Exception {

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    compiler.setGimpleDirectory(new File("target/test-gimple"));
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setWorkDirectory(new File("target/gnur-work"));
    compiler.setPackageName("org.renjin.gnur.test");
    compiler.setClassName("org.renjin.gnur.test.Mass");
    compiler.setVerbose(true);
    
    File srcRoot = new File("src/test/resources/org/renjin/gnur");
    
    compiler.addSources(new File(srcRoot, "mass"));

    compiler.compile();

  }

  @Ignore("Getting there...")
  @Test
  public void testZoo() throws Exception {

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    compiler.setGimpleDirectory(new File("target/test-gimple"));
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setWorkDirectory(new File("target/gnur-work"));
    compiler.setPackageName("org.renjin.gnur.test");
    compiler.setClassName("org.renjin.gnur.test.Zoo");
    compiler.setVerbose(true);

    File srcRoot = new File("src/test/resources/org/renjin/gnur");

    compiler.addSources(new File(srcRoot, "zoo"));

    compiler.compile();


  }

}
