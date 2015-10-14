package org.renjin.maven;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.renjin.packaging.PackageDescription;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

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
	 * Directory containing R sources
   *
	 * @parameter expression="src/main/R"
	 * @required
	 */
	private File sourceDirectory;

  /**
   * Directory containing data files
   * @parameter expression="src/main/data"
   */
  private File dataDirectory;

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
	 * @parameter expression="${project.artifactId}"
	 * @required
	 */
	private String packageName;

  /**
   * @parameter expression="${project.groupId}"
   * @required
   */
  private String groupId;


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

  /**
   * @parameter expression="${project.basedir}/DESCRIPTION"
   * @required
   */
  private File descriptionFile;


  /**
   * @parameter
   */
  private List<String> sourceFiles;


  /**
   * @parameter
   */
  private List defaultPackages;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
	  compileNamespaceEnvironment();
		copyNamespace();
    writeRequires();
		compileDatasets();
	}


  private void compileNamespaceEnvironment() throws MojoExecutionException {
	  
    ClassLoader classLoader = getClassLoader();
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(classLoader);

      Object builder = classLoader.loadClass("org.renjin.packaging.NamespaceBuilder").newInstance();
      builder.getClass()
          .getMethod("build", String.class, String.class, File.class, File.class, List.class, File.class, List.class)
          .invoke(builder, groupId, namespaceName, namespaceFile, sourceDirectory, sourceFiles, getEnvironmentFile(),
              defaultPackages);
     
    } catch(Exception e) {
      throw new MojoExecutionException("exception", e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextLoader);
    }
  }

  private void compileDatasets() throws MojoExecutionException {

    ClassLoader classLoader = getClassLoader();
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(classLoader);
      Constructor ctor = classLoader.loadClass("org.renjin.packaging.DatasetsBuilder")
          .getConstructor(File.class, File.class);
      Object builder = ctor.newInstance(getPackageRoot(), dataDirectory);
      builder.getClass().getMethod("build").invoke(builder);
    } catch(Exception e) {
      throw new MojoExecutionException("exception", e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextLoader);
    }
    
  }
 
	private File getEnvironmentFile() {
    return new File(getPackageRoot(), "environment");
  }

  private File getNamespaceOutput() {
    return new File(getPackageRoot(), "NAMESPACE");
  }

  private File getPackageRoot() {
    File packageRoot = new File(outputDirectory.getAbsoluteFile() + File.separator + 
    		groupId.replace(".", File.separator) + File.separator + packageName);
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

  private void writeRequires() {
    // save a list of packages that are to be loaded onto the
    // global search path when this package is loaded

    if(descriptionFile.exists()) {
      PackageDescription description;
      try {
        description = PackageDescription.fromFile(descriptionFile);
      } catch(IOException e) {
        throw new RuntimeException("Exception reading DESCRIPTION file");
      }
      try {
        PrintWriter requireWriter = new PrintWriter(new File(getPackageRoot(), "requires"));
        for(PackageDescription.PackageDependency dep : description.getDepends()) {
          if(!dep.getName().equals("R") && !Strings.isNullOrEmpty(dep.getName())) {
            requireWriter.println(dep.getName());
          }
        }
        requireWriter.close();
      } catch (IOException e) {
        throw new RuntimeException("Exception writing requires file", e);
      }
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

  private Set<Artifact> getDependencies() {
    Set<Artifact> artifacts = Sets.newHashSet();
    artifacts.addAll(project.getCompileArtifacts());
    artifacts.addAll(pluginDependencies);
    return artifacts; 
  }
}
