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

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.renjin.aether.AetherFactory;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.PackageBuilder;
import org.renjin.packaging.PackageSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Builds an R package
 */
public class Builder {

  private Builder() { }

  public static void execute(String action, String[] args) throws IOException {

    List<String> packagePaths = Arrays.asList(args);
    for (String packagePath : packagePaths) {
      PackageSource source = new PackageSource.Builder(packagePath)
          .setDefaultGroupId("org.renjin.cran")
          .build();

      PackageBuild build = new PackageBuild(source);

      if(action.equals("build") || action.equals("install")) {
        executeBuild(build);
      }
      if(action.equals("install")) {
        executeInstall(source, build);
      }
    }
  }

  private static void executeBuild(PackageBuild build) throws IOException {
    PackageBuilder builder = new PackageBuilder(build.getSource(), build);
    builder.build();

    PomBuilder pomBuilder = new PomBuilder(build, build.getSource().getVersion());
    pomBuilder.writePomFile();
    pomBuilder.writePomProperties();

    try(JarArchiver archiver = new JarArchiver(build.getJarFile())) {
      archiver.addDirectory(build.getOutputDir());
    } catch (Exception e) {
      throw new BuildException("Failed to create package jar", e);
    }
  }


  private static void executeInstall(PackageSource source, PackageBuild build) {
    RepositorySystem system = AetherFactory.newRepositorySystem();
    RepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);

    Artifact jarArtifact = new DefaultArtifact( source.getGroupId(), source.getPackageName(), "jar", build.getBuildVersion());
    jarArtifact = jarArtifact.setFile(build.getJarFile());

    Artifact pomArtifact = new SubArtifact( jarArtifact, "", "pom" );
    pomArtifact = pomArtifact.setFile( new File(build.getMavenMetaDir(), "pom.xml"));

    InstallRequest installRequest = new InstallRequest();
    installRequest.addArtifact( jarArtifact ).addArtifact( pomArtifact );

    try {
      system.install( session, installRequest );
    } catch (InstallationException e) {
      throw new BuildException("Exception installing artifact " + build.getJarFile().getAbsolutePath(), e);
    }
  }
}
