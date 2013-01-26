package org.renjin.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.google.common.collect.Lists;

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
   * @parameter expression="${project.artifactId}"
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
   * @parameter default-value="${project.build.directory}/renjin-test-reports"
   * @required
   */
  private File reportsDirectory;

  /**
   * @parameter expression="${skipTests}" default-value="false"
   */
  private boolean skipTests;
  
  
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
    
    ClassLoader classLoader = getClassLoader();
    try {
      Object runner = classLoader.loadClass("org.renjin.maven.test.TestRunner").newInstance();
      boolean succeeded = (Boolean)runner.getClass()
          .getMethod("run", File.class, File.class, String.class, List.class)
          .invoke(runner, testSourceDirectory, reportsDirectory, namespaceName, defaultPackages);
           
      if(!succeeded && !testFailureIgnore) {
        throw new MojoFailureException("There were R test failures");
      }
    } catch(Exception e) {
      throw new MojoExecutionException("exception", e);
    }
  }

  private ClassLoader getClassLoader() throws MojoExecutionException  {
    try {
      getLog().debug("Renjin Test Classpath: ");
      
      List<URL> classpathURLs = Lists.newArrayList();
      classpathURLs.add( new File(project.getBuild().getOutputDirectory()).toURI().toURL() );
      
      for(Artifact artifact : getDependencies()) {
        getLog().debug("  "  + artifact.getFile());
        
        classpathURLs.add(artifact.getFile().toURI().toURL());
      }   
      
      return new URLClassLoader( classpathURLs.toArray( new URL[ classpathURLs.size() ] ) );
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
