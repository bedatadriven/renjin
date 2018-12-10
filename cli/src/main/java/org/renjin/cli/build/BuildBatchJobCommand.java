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

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.renjin.RenjinVersion;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.PackageBuilder;
import org.renjin.packaging.PackageSource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Command(name = "build", description = "Build a self-contained batch job from a package")
public class BuildBatchJobCommand extends BuildCommand {

  @Option(name = "--renjin-version", description = "The version of Renjin to include")
  public String renjinVersion = RenjinVersion.getVersionName();

  @Arguments(description = "Path to package source")
  public String packageSource = ".";

  @Override
  protected void tryRun() throws Exception {

    PackageSource source = new PackageSource.Builder(packageSource).build();
    PackageBuild build = new PackageBuild(source, Optional.of(renjinVersion));

    PackageBuilder builder = new PackageBuilder(build.getSource(), build);
    builder.build();

    buildFatJar(build);
  }

  static void buildFatJar(PackageBuild build) throws IOException {
    try(JarArchiver archiver = new JarArchiver(build.getFatJarFile(), executableNamespace(build))) {
      archiver.addDirectory(build.getOutputDir());

      for (File file : build.getDependencyResolution().getArtifacts()) {
        archiver.addClassesFromJar(file);
      }
    } catch (Exception e) {
      throw new BuildException("Failed to create package jar", e);
    }
  }


  private static Optional<String> executableNamespace(PackageBuild build) {
    if(build.getExecuteMetadataFile().exists()) {
      return Optional.of(build.getSource().getFqName().toString());
    } else {
      return Optional.empty();
    }
  }
}
