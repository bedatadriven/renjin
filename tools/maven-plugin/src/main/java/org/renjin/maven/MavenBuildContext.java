package org.renjin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.gnur.GnurInstallation;
import org.renjin.packaging.BuildContext;
import org.renjin.packaging.BuildLogger;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class MavenBuildContext implements BuildContext {

  private MavenProject project;
  
  private MavenBuildLogger logger = new MavenBuildLogger();

  private File buildDir;
  private File outputDir;
  private File pluginFile;
  private File homeDir;
  private File unpackedIncludeDir;
  private URL[] classpath;
  
  private Collection<Artifact> pluginDependencies;

  public MavenBuildContext(MavenProject project, Collection<Artifact> pluginDependencies) throws MojoExecutionException {
    this.project = project;
    this.buildDir = new File(project.getBuild().getDirectory());
    this.outputDir = new File(project.getBuild().getOutputDirectory());
    this.pluginDependencies = pluginDependencies;
    this.homeDir = new File(buildDir, "gnur");
    this.pluginFile = new File(buildDir, "bridge.so");
    this.unpackedIncludeDir = new File(buildDir, "include");
    this.classpath = buildClassPath();
  }


  public void setupNativeCompilation() throws MojoExecutionException {
    // Unpack any headers from dependencies
    GccBridgeHelper.unpackHeaders(logger.getLog(), unpackedIncludeDir, project.getCompileArtifacts());
    try {
      GnurInstallation.unpackRHome(homeDir);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to unpack GNU R installation", e);
    }
    try {
      Gcc.extractPluginTo(pluginFile);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to unpack GCC Bridge Plugin", e);
    }
  }
  
  @Override
  public BuildLogger getLogger() {
    return logger;
  }

  @Override
  public File getGccBridgePlugin() {
    return pluginFile;
  }

  @Override
  public File getGnuRHomeDir() {
    return homeDir;
  }

  @Override
  public File getUnpackedIncludesDir() {
    return unpackedIncludeDir;
  }

  @Override
  public File getOutputDir() {
    return outputDir;
  }

  @Override
  public File getPackageOutputDir() {
    return buildDir;
  }

  @Override
  public ClassLoader getClassLoader() {
    return new URLClassLoader(classpath);
  }

  private URL[] buildClassPath() throws MojoExecutionException  {
    try {
      logger.debug("Renjin Evaluation Classpath: ");

      Set<Artifact> artifacts = Sets.newHashSet();
      artifacts.addAll(project.getCompileArtifacts());
      artifacts.addAll(pluginDependencies);

      List<URL> classpathURLs = Lists.newArrayList();
      
      // Add the output directory to the classpath so that the compiled 
      // package can be loaded if needed by data scripts.
      // N.B.: directory URLs *must* end in a '/', otherwise they're considered
      // jar entries.
      File outputDir = new File(project.getBuild().getOutputDirectory());
      classpathURLs.add(new URL("file", null, outputDir.getAbsolutePath() + "/"));

      for(Artifact artifact : artifacts) {
        classpathURLs.add(artifact.getFile().toURI().toURL());
      }

      URL[] array = classpathURLs.toArray(new URL[classpathURLs.size()]);
      for (URL url : array) {
        logger.debug("  "  + url.toString());
      }
      
      return array;
      
    } catch(MalformedURLException e) {
      throw new MojoExecutionException("Exception resolving classpath", e);
    }
  }
}
