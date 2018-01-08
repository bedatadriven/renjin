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
import org.renjin.maven.test.ForkedTestController;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Run R tests
 */
@Mojo(name = "test", 
      defaultPhase = LifecyclePhase.TEST, 
      requiresDependencyResolution = ResolutionScope.TEST)
public class TestMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.groupId}:${project.artifactId}", required = true)
  private String namespaceName;


  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
  private List<Artifact> pluginDependencies;


  /**
   * The enclosing project.
   */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${project.basedir}/src/test/R", required = true)
  private File testSourceDirectory;

  @Parameter(defaultValue = "${project.basedir}/man", required = true)
  private File documentationDirectory;

  @Parameter(defaultValue = "${project.build.directory}/renjin-test-reports", required = true)
  private File reportsDirectory;

  @Parameter(property = "skipTests", defaultValue = "false")
  private boolean skipTests;
  
  @Parameter(defaultValue = "51200")
  private int outputLimit;

  /**
   * Kill the forked test process after a certain number of seconds. If set to 0, wait forever 
   * for the process, never timing out.
   * 
   */
  @Parameter
  private int timeoutInSeconds;

  @Parameter
  private List defaultPackages;

  @Parameter(property = "maven.test.failure.ignore", defaultValue = "false")
  private boolean testFailureIgnore;


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    if(skipTests) {
      getLog().info("Skipping Renjin tests.");
      return;
    }

    if(defaultPackages == null) {
      defaultPackages = Lists.newArrayList();
    }

    ForkedTestController controller = new ForkedTestController();
    controller.setTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    controller.setDefaultPackages(defaultPackages);
    controller.setClassPath(buildClassPath());
    controller.setNamespaceUnderTest(namespaceName);
    controller.setOutputLimit(outputLimit);
    controller.setTestReportDirectory(reportsDirectory);
    
    controller.executeTests(testSourceDirectory);
    controller.executeTests(documentationDirectory);
    controller.shutdown();
    
    if(!controller.allTestsSucceeded()) {
      if(testFailureIgnore) {
        System.err.println("There were R test failures.");
      } else {
        throw new MojoFailureException("There were R test failures");
      }
    }
  }


  private String buildClassPath() throws MojoExecutionException  {
    try {
      getLog().debug("Renjin Test Classpath: ");

      List<URL> classpathURLs = Lists.newArrayList();
      classpathURLs.add( new File(project.getBuild().getOutputDirectory()).toURI().toURL() );

      for(Artifact artifact : getDependencies()) {
        getLog().debug("  "  + artifact.getFile());

        classpathURLs.add(artifact.getFile().toURI().toURL());
      }

      return Joiner.on(File.pathSeparator).join(classpathURLs);

    } catch(MalformedURLException e) {
      throw new MojoExecutionException("Exception resolving classpath", e);
    }
  }

  private List<Artifact> getDependencies() {
    List<Artifact> artifacts = Lists.newArrayList();
    artifacts.addAll(project.getTestArtifacts());
    artifacts.addAll(pluginDependencies);
    return artifacts;
  }

}
