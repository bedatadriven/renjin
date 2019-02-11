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
package org.renjin.cli.build;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.PackageBuilder;

import java.io.IOException;
import java.util.Optional;



/**
 * Builds an R package
 */
public class Builder {

  public static void execute(String... args) throws IOException {

    Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("renjin")
        .withDefaultCommand(Help.class)
        .withCommand(Help.class);

    builder.withGroup("package")
        .withDescription("Build an R package JAR from source")
        .withDefaultCommand(PackageBuildCommand.class)
        .withCommands(PackageInstallCommand.class, PackageBuildCommand.class, PackageMavenizeCommand.class);

    builder.withGroup("batch-job")
        .withDescription("Build and deploy batch jobs to Renjin Batch Server")
        .withDefaultCommand(Help.class)
        .withCommand(BuildBatchJobCommand.class);

    Cli<Runnable> cli = builder.build();

    cli.parse(args).run();

  }

  static void buildPackage(PackageBuild build) throws IOException {
    PackageBuilder builder = new PackageBuilder(build.getSource(), build);
    builder.build();
  }

  static void buildJar(PackageBuild build) throws IOException {

    writePomFile(build);

    try(JarArchiver archiver = new JarArchiver(build.getJarFile(), Optional.empty())) {
      archiver.addDirectory(build.getOutputDir());

    } catch (Exception e) {
      throw new BuildException("Failed to create package jar", e);
    }
  }

  static void writePomFile(PackageBuild build) {
    PomBuilder pomBuilder = new PomBuilder(build);
    pomBuilder.writePomFile();
    pomBuilder.writePomProperties();
  }



}
