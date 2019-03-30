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

package org.renjin.gcc;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.repackaged.guava.base.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple build driver that is used by other projects in the
 * Renjin multi-project.
 */
public class Build {

  public static void main(String[] args) throws Exception {
    Gcc gcc = new Gcc();
    gcc.extractPlugin();
    gcc.setDebug(true);
    gcc.setGimpleOutputDir(new File("build/gcc-bridge/gimple"));
    gcc.addCFlags(getFlags("CFLAGS"));

    List<GimpleCompilationUnit> units = new ArrayList<>();

    for (File cSource : findCSources()) {
      units.add(gcc.compileToGimple(cSource));
    }

    for (File fortranSource : findFortranSources()) {
      units.add(gcc.compileToGimple(fortranSource));
    }

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.addMathLibrary();
    compiler.setPackageName(System.getenv("PACKAGE"));
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.setClassName(System.getenv("CLASS"));
    compiler.setOutputDirectory(new File("build/gcc-bridge/classes"));
    compiler.setLoggingDirectory(new File("build/gcc-bridge/logs"));
    compiler.compile(units);
  }

  private static List<File> findCSources() {
    File sourceDir = new File("src/main/c");
    List<File> sources = new ArrayList<>();
    if(sourceDir.exists()) {
      for (File file : sourceDir.listFiles()) {
        if(file.getName().endsWith(".c")) {
          sources.add(file);
        }
      }
    }
    return sources;
  }
  private static List<File> findFortranSources() {
    File sourceDir = new File("src/main/fortran");
    List<File> sources = new ArrayList<>();
    if(sourceDir.exists()) {
      for (File file : sourceDir.listFiles()) {
        if(file.getName().endsWith(".f")) {
          sources.add(file);
        }
      }
    }
    return sources;
  }

  private static List<String> getFlags(String name) {
    String property = System.getenv(name);
    if(Strings.isNullOrEmpty(property)) {
      return Collections.emptyList();
    }
    return Arrays.asList(property.split("\\s+"));
  }

}
