package org.renjin.maven;

import java.io.File;
import java.lang.reflect.Method;
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
 * @goal repl
 * @requiresDependencyResolution test
 * @requiresProject true
 */
public class ReplMojo extends AbstractMojo {


  /**
    * The enclosing project.
    * 
    * @parameter default-value="${project}"
    * @required
    * @readonly
    */
  private MavenProject project;
  

  /**
    * @parameter default-value="${plugin.artifacts}"
    * @readonly
    * @since 1.1-beta-1
    */
  private List<Artifact> pluginDependencies;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    ClassLoader loader = getClassLoader();
    try {
      Class replClass = loader.loadClass("org.renjin.repl.JlineRepl");
      Method mainMethod = replClass.getMethod("main", String[].class);
      mainMethod.invoke(null, new Object[] { null } );
    } catch(Exception e) {
      throw new MojoExecutionException("Repl error", e);
    }
  }
  

  private ClassLoader getClassLoader() throws MojoExecutionException  {
    try {
      getLog().debug("Renjin REPL Classpath: ");
      
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
