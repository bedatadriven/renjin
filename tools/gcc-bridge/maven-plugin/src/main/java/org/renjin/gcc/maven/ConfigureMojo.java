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
package org.renjin.gcc.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.renjin.gcc.Gcc;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes a "configure" script with arguments necessary for gcc-bridge
 */
@ThreadSafe
@Mojo(name = "configure",  requiresDependencyCollection = ResolutionScope.COMPILE)
public class ConfigureMojo extends AbstractMojo {

  @Parameter( defaultValue = "${basedir}/configure")
  private File configureScriptFile;

  @Parameter( defaultValue = "${basedir}/bridge.so")
  private File pluginFile;

  @Parameter
  private List<String> configureArgs;

  @Parameter(name = "configure.force")
  private boolean force;

  @Parameter(defaultValue = "${basedir}/Makefile")
  private File outputFile;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    if(!force && outputFile.exists()) {
      getLog().info(outputFile + " exists, skipping.");
      return;
    }

    if(!configureScriptFile.exists()) {
      throw new MojoFailureException("Configure script does not exist at " + configureScriptFile.getAbsolutePath());
    }
    if(!configureScriptFile.canExecute()) {
      throw new MojoFailureException("Configure script is not executable");
    }

    List<String> command = new ArrayList<>();
    command.add(configureScriptFile.getAbsolutePath());
    if(configureArgs != null) {
      command.addAll(configureArgs);
    }

    try {
      Gcc.extractPluginTo(pluginFile);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to extract GCC-Bridge GCC plugin to " + pluginFile.getAbsolutePath(), e);
    }

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(configureScriptFile.getParentFile());
    builder.environment().put("CC", "gcc-4.7");
    builder.environment().put("CFLAGS", "-m32 -fplugin=" + pluginFile.getAbsolutePath());
    builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    builder.redirectError(ProcessBuilder.Redirect.INHERIT);
    int exitCode = 0;
    try {
      exitCode = builder.start().waitFor();
    } catch (InterruptedException e) {
      throw new MojoFailureException("./configure execution interrupted.");
    } catch (IOException e) {
      throw new MojoExecutionException("./configure execution failed", e);
    }
    if(exitCode != 0) {
      throw new MojoFailureException("./configure failed with exit code " + exitCode);
    }
  }
}
