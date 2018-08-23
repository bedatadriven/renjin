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
package org.renjin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.gnur.GnurSourcesCompiler;
import org.renjin.repackaged.guava.collect.Lists;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.List;

/**
 * Compiles gnur C/Fortran sources to a JVM class

 */
@ThreadSafe
@Mojo(name = "gnur-sources-compile", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurCompilerMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Component
  private MavenProjectHelper projectHelper;

  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
  private List<Artifact> pluginDependencies;
  
  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;

  /**
   * Directory to which the intermediate gimple files are written
   */
  @Parameter(defaultValue = "${project.build.directory}/gimple", required = true)
  private File gimpleDirectory;

  /**
   * Directory to which the intermediate gimple files are written
   */
  @Parameter(defaultValue = "${project.build.directory}/gcc-bridge-logs", required = true)
  private File loggingDirectory;

  /**
   * Directories in which to look for C/Fortran sources
   */
  @Parameter
  private List<File> sourceDirectories;

  /**
   * If true, do not fail the build if compilation fails.
   */
  @Parameter(property = "ignore.gnur.compilation.failure", defaultValue = "false")
  private boolean ignoreFailure;

  /**
   * Scratch directory for GCC output/files
   */
  @Parameter(defaultValue = "${project.build.directory}/gcc-work")
  private File workDirectory;

  @Parameter
  private List<File> includeDirectories;

  @Parameter(defaultValue = "${project.build.finalName}")
  private String finalName;

  @Parameter(defaultValue = "${project.build.directory}/include")
  private File unpackedIncludeDir;

  @Parameter
  private boolean transformGlobalVariables;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    if (sourceDirectories == null || sourceDirectories.isEmpty()) {
      sourceDirectories = Lists.newArrayList(sourceDir("c"), sourceDir("fortran"));
    }

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    for (File sourceDir : sourceDirectories) {
      compiler.addSources(sourceDir);
    }

    workDirectory.mkdirs();
    gimpleDirectory.mkdirs();
    outputDirectory.mkdirs();

    compiler.setVerbose(false);
    compiler.setPackageName(project.getGroupId() + "." + project.getArtifactId());
    compiler.setClassName(project.getArtifactId());
    compiler.setWorkDirectory(workDirectory);
    compiler.setOutputDirectory(outputDirectory);
    compiler.setGimpleDirectory(gimpleDirectory);
    compiler.setLinkClassLoader(GccBridgeHelper.getLinkClassLoader(project, getLog()));
    compiler.setLoggingDir(loggingDirectory);
    compiler.setTransformGlobalVariables(transformGlobalVariables);

    
    // Unpack any headers from dependencies
    GccBridgeHelper.unpackHeaders(getLog(), unpackedIncludeDir, project.getCompileArtifacts());
    compiler.addIncludeDir(unpackedIncludeDir);

    // Add the standard GNU R inst/include to the compiler's
    // include path if it exists
    File instDir = new File(project.getBasedir(), "inst");
    File instIncludeDir = new File(instDir, "include");
    if (instIncludeDir.exists()) {
      getLog().info("Adding " + instIncludeDir.getAbsolutePath() + " to include path...");
      compiler.addIncludeDir(instIncludeDir);
    }

    if (includeDirectories != null) {
      for (File includeDirectory : includeDirectories) {
        compiler.addIncludeDir(includeDirectory);
      }
    }
  
    try {
      compiler.compile();
      getLog().info("Compilation of GNU R sources succeeded.");
    } catch (Exception e) {
      if (ignoreFailure) {
        getLog().error("Compilation of GNU R sources failed.");
        e.printStackTrace(System.err);
      } else {
        throw new MojoExecutionException("Compilation of GNU R sources failed", e);
      }
    }

    if (instIncludeDir.exists()) {
      GccBridgeHelper.archiveHeaders(getLog(), project, instIncludeDir);
    }
  }

  private File sourceDir(String subDirectory) {
    return new File(project.getBasedir().getAbsolutePath() + 
        File.separator + "src" + 
        File.separator + "main" +
        File.separator + subDirectory);
  }

  private List<File> pluginDependencies() {
    List<File> paths = Lists.newArrayList();
    for (Artifact artifact : pluginDependencies) {
      paths.add(artifact.getFile());
    }
    return paths;
  }

}