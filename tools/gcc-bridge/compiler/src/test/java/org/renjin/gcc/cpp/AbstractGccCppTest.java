/**
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
package org.renjin.gcc.cpp;

import org.renjin.gcc.AbstractGccTest;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.lib.cpp.CppSymbolLibrary;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AbstractGccCppTest extends AbstractGccTest {

  @Override 
  protected void compileGimple(List<GimpleCompilationUnit> units) throws Exception {
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setRecordClassPrefix(units.get(0).getName());
    compiler.setPackageName(PACKAGE_NAME);
    compiler.setVerbose(true);
    compiler.addLibrary(new CppSymbolLibrary());
    compiler.compile(units);
  }

  public List<GimpleCompilationUnit> compileToGimple(List<String> sources) throws IOException {
    File workingDir = new File("target/gcc-work");
    workingDir.mkdirs();

    Gcc gcc = new Gcc(workingDir);
    if(Strings.isNullOrEmpty(System.getProperty("gcc.bridge.plugin"))) {
      gcc.extractPlugin();
    } else {
      gcc.setPluginLibrary(new File(System.getProperty("gcc.bridge.plugin")));
    }
    gcc.setDebug(true);
    gcc.setGimpleOutputDir(new File("target/gimple"));


    List<GimpleCompilationUnit> units = Lists.newArrayList();

    for (String sourceName : sources) {
      File source = new File(AbstractGccCppTest.class.getResource(sourceName).getFile());
      GimpleCompilationUnit unit = gcc.compileToGimple(source);
      units.add(unit);
    }
    return units;
  }
}