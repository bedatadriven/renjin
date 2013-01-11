package org.renjin.maven;

import java.io.File;
import java.io.IOException;
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
import com.google.common.io.Files;

/**
 * Compiles R sources into a serialized blob
 * 
 * @goal namespace-compile
 * @phase compile
 * @requiresProject true
 * @requiresDependencyResolution compile

 */
public class NamespaceMojo extends AbstractMojo {

	/**
	 * Name of the R package
	 * @parameter expression="src/main/R"
	 * @required
	 */
	private File sourceDirectory;	

	/**
	 * Name of the R package
	 * @parameter expression="${project.build.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private File outputDirectory;	
	
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
	 * @parameter expression="${project.groupId}.${project.artifactId}"
	 * @required
	 */
	private String packageName;

	/**
   * @parameter expression="${project.artifactId}"
   * @required
	 */
	private String namespaceName;
	
	/**
	 * @parameter expression="${project.basedir}/NAMESPACE"
	 * @required
	 */
	private File namespaceFile;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
	  compileNamespaceEnvironment();
		copyNamespace();
	}
	
	private void compileNamespaceEnvironment() throws MojoExecutionException {
	  
    ClassLoader classLoader = getClassLoader();
    try {
      Object builder = classLoader.loadClass("org.renjin.maven.namespace.NamespaceBuilder").newInstance();
      builder.getClass()
          .getMethod("build", String.class, File.class, File.class)
          .invoke(builder, namespaceName, sourceDirectory, getEnvironmentFile());
     
    } catch(Exception e) {
      throw new MojoExecutionException("exception", e);
    }    
  }
	
 
	private File getEnvironmentFile() {
    return new File(getPackageRoot(), "environment");
  }

  private File getNamespaceOutput() {
    return new File(getPackageRoot(), "NAMESPACE");
  }

  private File getPackageRoot() {
    File packageRoot = new File(outputDirectory.getAbsoluteFile() + File.separator + packageName.replace(".", File.separator));
    packageRoot.mkdirs();
    return packageRoot;
  }


  private void copyNamespace() {
    try {
      if(!namespaceFile.exists()) {
        System.err.println("NAMESPACE file is missing. (looked in " + namespaceFile.getAbsolutePath() + ")");
        throw new RuntimeException("Missing NAMESPACE file");
      }
      Files.copy(namespaceFile, getNamespaceOutput());
    } catch (IOException e) {
      throw new RuntimeException("Exception copying NAMESPACE file", e);
    }
  }
  
  

  private ClassLoader getClassLoader() throws MojoExecutionException  {
    try {
      getLog().debug("Renjin Namespace Evaluation Classpath: ");
      
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
    artifacts.addAll(project.getCompileArtifacts());
    artifacts.addAll(pluginDependencies);
    return artifacts; 
  }
}
