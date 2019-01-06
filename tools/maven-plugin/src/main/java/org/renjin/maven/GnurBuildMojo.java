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
package org.renjin.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.packaging.PackageBuilder;
import org.renjin.packaging.PackageSource;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.Collections;

/**
 * Builds a complete package laid out according to GNU R conventions.
 */
@ThreadSafe
@Mojo(name = "gnur-compile", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurBuildMojo extends AbstractMojo {


  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /**
   * If true, do not fail the build if compilation fails.
   */
  @Parameter(property = "ignore.gnur.compilation.failure", defaultValue = "false")
  private boolean ignoreFailure;


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    try {
      PackageSource packageSource = new PackageSource.Builder(project.getBasedir())
          .setGroupId(project.getGroupId())
          .setPackageName(project.getArtifactId())
          .build();
      
      MavenBuildContext buildContext = new MavenBuildContext(project, Collections.emptySet(), getLog());
      buildContext.setDefaultPackagesIfDependencies();
      
      PackageBuilder builder = new PackageBuilder(packageSource, buildContext);
      builder.setIgnoreNativeCompilationFailure(ignoreFailure);
      builder.build();

      archiveHeaders(buildContext);


    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    
  }


  private void archiveHeaders(MavenBuildContext buildContext) throws MojoExecutionException {

    File stagingIncludes = new File(buildContext.getPackageOutputDir(), "include");
    
    if(stagingIncludes.exists()) {
      GccBridgeHelper.archiveHeaders(getLog(), project, stagingIncludes);
    }
  }
  
}
