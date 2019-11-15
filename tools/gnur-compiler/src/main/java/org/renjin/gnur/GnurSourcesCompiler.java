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
package org.renjin.gnur;


import io.airlift.airline.*;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiles the native code of a GNU R package, applying required code transformations
 * for compatibility with Renjin.
 */
@Command(name = "compile")
public class GnurSourcesCompiler {

  @Inject
  public HelpOption helpOption;

  @Option(name = "--package", required = true, description = "The package of the compiled sources")
  private String packageName;

  @Option(name = "--class", required = true, description = "The name of the trampoline class")
  private String className;

  @Option(name = "--verbose")
  private boolean verbose = true;

  @Option(name = "--transform-global-variables", description = "Transform global variables to session-scoped variables.")
  private boolean transformGlobalVariables;

  @Option(name = "--output-dir", required = true, description = "The directory into which class files will be written")
  private File outputDirectory = new File("build/classes");

  @Option(name = "--input-dir", required = true, description = "The directory containing the .gimple files to compile")
  private File gimpleDirectory = new File("build/gimple");

  @Option(name = "--logging-dir", description = "The directory to which compilation logs should be written")
  private File loggingDir;

  private ClassLoader linkClassLoader = getClass().getClassLoader();

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
  
  public void setClassName(String className) {
    this.className = className;
  }

  public void setGimpleDirectory(File gimpleDirectory) {
    this.gimpleDirectory = gimpleDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void setLoggingDir(File loggingDir) {
    this.loggingDir = loggingDir;
  }

  public void setTransformGlobalVariables(boolean transformGlobalVariables) {
    this.transformGlobalVariables = transformGlobalVariables;
  }

  public void compile() throws Exception {

    List<GimpleCompilationUnit> units = new ArrayList<>();

    collectGimple(gimpleDirectory, units);

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(outputDirectory);
    compiler.setPackageName(packageName);
    compiler.setClassName(className);
    compiler.setVerbose(verbose);

    compiler.setLinkClassLoader(linkClassLoader);
    compiler.addMathLibrary();

    setupCompiler(compiler);

    if (transformGlobalVariables) {
      compiler.addPlugin(new GlobalVarPlugin(compiler.getPackageName()));
    }

    compiler.setLoggingDirectory(loggingDir);

    compiler.compile(units);
  }

  public static void setupCompiler(GimpleCompiler compiler) {
    compiler.addTransformer(new SetTypeRewriter());
    compiler.addTransformer(new MutationRewriter());
  }

  private void collectGimple(File dir, List<GimpleCompilationUnit> gimpleFiles) throws IOException {

    GimpleParser parser = new GimpleParser();

    if(!dir.isDirectory() && dir.getName().endsWith(".zip")) {
      gimpleFiles.addAll(parser.parseZipFile(dir));
      return;
    }

    File[] files = dir.listFiles();
    if(files != null) {
      for (File file : files) {
        if(file.getName().endsWith(".gimple")) {
          try {
            gimpleFiles.add(parser.parse(file));
          } catch (IOException e) {
            throw new IOException("Failed to parse gimple file " + file, e);
          }
        } else if(file.getName().endsWith("gimple.zip")) {
          gimpleFiles.addAll(parser.parseZipFile(file));

        } else if(file.isDirectory()) {
          collectGimple(file, gimpleFiles);
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {

    SingleCommand<GnurSourcesCompiler> command =
        SingleCommand.singleCommand(GnurSourcesCompiler.class);

    GnurSourcesCompiler compiler;
    try {
      compiler = command.parse(args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.err.println();
      Help.help(command.getCommandMetadata());

      System.exit(-1);
      return;
    }

    if(compiler.helpOption.showHelpIfRequested()) {
      return;
    }

    compiler.compile();

  }
}
