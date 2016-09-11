package org.renjin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Starts an interactive Read-Eval-Print Loop with the project and its
 * dependencies on the classpath.
 */
@Mojo(name = "repl", requiresDependencyResolution = ResolutionScope.TEST)
public class ReplMojo extends AbstractMojo {


  /**
    * The enclosing project.
    */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;
  

  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
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
