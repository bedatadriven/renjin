package org.renjin.maven;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.renjin.maven.test.ForkedTestController;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Run R tests
 *
 * @goal test
 * @phase test
 * @requiresProject true
 * @requiresDependencyResolution test
 */
public class TestMojo extends AbstractMojo {

  /**
   * @parameter expression="${project.groupId}:${project.artifactId}"
   * @required
   */
  private String namespaceName;


  /**
   * @parameter default-value="${plugin.artifacts}"
   * @readonly
   * @since 1.1-beta-1
   */
  private List<Artifact> pluginDependencies;


  /**
   * The enclosing project.
   *
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * @parameter default-value="${project.basedir}/src/test/R"
   * @required
   */
  private File testSourceDirectory;

  /**
   * @parameter default-value="${project.basedir}/man"
   */
  private File documentationDirectory;

  /**
   * @parameter default-value="${project.build.directory}/renjin-test-reports"
   * @required
   */
  private File reportsDirectory;

  /**
   * @parameter expression="${skipTests}" default-value="false"
   */
  private boolean skipTests;


  /**
   * Kill the forked test process after a certain number of seconds. If set to 0, wait forever 
   * for the process, never timing out.
   * 
   * @parameter
   */
  private int timeoutInSeconds;

  /**
   * @parameter
   */
  private List defaultPackages;

  /**
   * @parameter expression="${maven.test.failure.ignore}" default-value="false"
   */
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
