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

import io.airlift.airline.Command;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.renjin.aether.AetherFactory;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.PackageSource;

import java.io.File;

@Command(name = "install", description = "Install a package to the local Maven repository")
public class PackageInstallCommand extends PackageCommand {


  @Override
  protected void tryRun() throws Exception {

    PackageBuild build = createBuildContext();

    Builder.buildPackage(build);
    Builder.buildJar(build);
    installPackage(build);
  }

  private static void installPackage(PackageBuild build) {
    RepositorySystem system = AetherFactory.newRepositorySystem();
    RepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);

    PackageSource source = build.getSource();
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
