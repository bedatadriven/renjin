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
package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class TrampolineClassTest extends AbstractGccTest {

  @Test
  public void test() throws Exception {

    List<GimpleCompilationUnit> units = compileToGimple(Arrays.asList("link1.c", "link2.c"));

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.addRecordClass("jvm_rect", JvmRect.class);
    compiler.addMethod("jvm_area", JvmRect.class, "area");
    compiler.setClassName("Linked");
    compiler.compile(units);
    
    Class<?> clazz = Class.forName("org.renjin.gcc.Linked");
    Method externMethod = clazz.getMethod("shared_triple", int.class);

    int result = (Integer)externMethod.invoke(null, 3);

    assertThat(result, equalTo(9));

  }


}
