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
package org.renjin.gcc.maven;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.HtmlTreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ThreadSafe
@Mojo(name = "make",  requiresDependencyCollection = ResolutionScope.COMPILE)
public class MakeMojo extends AbstractMojo {

  @Parameter(defaultValue = "${basedir}")
  private File baseDir;

  @Parameter( defaultValue = "${project.build.outputDirectory}")
  private File outputDirectory;


  @Parameter( defaultValue = "${project.build.directory}/gcc-bridge-logs")
  private File loggingDir;

  @Parameter(defaultValue = "false")
  private boolean generateJavadoc;

  @Parameter (defaultValue = "${project.build.directory}/gcc-bridge-javadoc")
  private File javadocOutputDirectory;

  @Parameter(required = true, defaultValue = "${project.groupId}")
  private String packageName;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    executeMake();

    List<File> gimpleSources = new ArrayList<>();
    findGimpleOutput(gimpleSources, baseDir);
    compileGimple(gimpleSources);
  }


  private void executeMake() throws MojoExecutionException, MojoFailureException {
    ProcessBuilder processBuilder = new ProcessBuilder("make");
    processBuilder.directory(baseDir);
    processBuilder.inheritIO();

    Process process;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to execute 'make'", e);
    }

    int exitCode = 0;
    try {
      exitCode = process.waitFor();
    } catch (InterruptedException e) {
      throw new MojoFailureException("'make' interrupted.");
    }

    if(exitCode != 0) {
      throw new MojoFailureException("'make' failed with exit code " + exitCode);
    }
  }


  private void findGimpleOutput(List<File> gimpleSources, File dir) {
    for (File file : dir.listFiles()) {
      if(file.getName().endsWith(".gimple")) {
        gimpleSources.add(file);
      }
    }
  }


  private void compileGimple(List<File> gimpleSources) throws MojoFailureException, MojoExecutionException {

    List<GimpleCompilationUnit> units = new ArrayList<>();

    GimpleParser parser = new GimpleParser();
    for (File gimpleSource : gimpleSources) {
      if(gimpleSource.length() != 0) {
        try {
          units.add(parser.parse(gimpleSource));
        } catch (IOException e) {
          throw new MojoFailureException("Failed to parse gimple output " + gimpleSource);
        }
      }
    }

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(outputDirectory);
    compiler.setPackageName(packageName);
    compiler.setLogger(new HtmlTreeLogger(loggingDir));
    try {
      compiler.compile(units);
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to compile gimple", e);
    }
  }

}
