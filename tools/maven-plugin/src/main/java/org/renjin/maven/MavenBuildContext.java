package org.renjin.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.gnur.GnurInstallation;
import org.renjin.packaging.BuildContext;
import org.renjin.packaging.BuildLogger;

import java.io.File;
import java.io.IOException;


public class MavenBuildContext implements BuildContext {

  private MavenProject project;
  
  private MavenBuildLogger logger = new MavenBuildLogger();

  private File buildDir;
  private File outputDir;
  private File pluginFile;
  private File homeDir;
  private File unpackedIncludeDir;
  
  public MavenBuildContext(MavenProject project) {
    this.project = project;
    this.buildDir = new File(project.getBuild().getDirectory());
    this.outputDir = new File(project.getBuild().getOutputDirectory());
    this.homeDir = new File(buildDir, "gnur");
    this.pluginFile = new File(buildDir, "bridge.so");
    this.unpackedIncludeDir = new File(buildDir, "include");
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
    throw new UnsupportedOperationException();
  }
}
