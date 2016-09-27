/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.packaging.PackageBuilder;
import org.renjin.packaging.PackageSource;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Compiles R sources into a serialized blob
 */
@ThreadSafe
@Mojo(name = "namespace-compile",
      defaultPhase = LifecyclePhase.COMPILE, 
      requiresDependencyResolution = ResolutionScope.COMPILE)
public class NamespaceMojo extends AbstractMojo {

  /**
   * Directory containing R sources
   *
   */
  @Parameter(defaultValue = "src/main/R", required = true)
  private File sourceDirectory;

  /**
   * Directory containing data files
   */
  @Parameter(defaultValue = "src/main/data")
  private File dataDirectory;

  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;

  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
  private List<Artifact> pluginDependencies;


  /**
   * The enclosing project.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;


  @Parameter(defaultValue = "${project.artifactId}", required = true)
  private String packageName;

  @Parameter(defaultValue = "${project.groupId}", required = true, readonly = true)
  private String groupId;

  @Parameter(defaultValue = "${project.artifactId}", required = true)
  private String namespaceName;

  @Parameter(defaultValue = "${project.basedir}/NAMESPACE")
  private File namespaceFile;

  @Parameter(defaultValue = "${project.basedir}/DESCRIPTION")
  private File descriptionFile;
  
  @Parameter
  private List<String> sourceFiles;

  @Parameter
  private List<String> defaultPackages;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    try {
      PackageSource source = new PackageSource.Builder(project.getBasedir())
          .setGroupId(groupId)
          .setPackageName(packageName)
          .setDescriptionFile(descriptionFile)
          .setNamespaceFile(namespaceFile)
          .setSourceDir(sourceDirectory)
          .setSourceFiles(sourceFiles)
          .setDataDir(dataDirectory)
          .build();

      MavenBuildContext buildContext = new MavenBuildContext(project, pluginDependencies, getLog());
      buildContext.setDefaultPackages(defaultPackages);

      PackageBuilder builder = new PackageBuilder(source, buildContext);
      builder.copyRootFiles();
      builder.compileNamespace();
      builder.compileDatasets();

    } catch (IOException e) {
      throw new MojoExecutionException("IOException: " + e.getMessage(), e);
    }
  }

}
