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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.gnur.GnurInstallation;
import org.renjin.packaging.BuildContext;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.BuildLogger;
import org.renjin.packaging.DefaultPackages;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class MavenBuildContext implements BuildContext {

  private MavenProject project;
  
  private MavenBuildLogger logger;

  private File buildDir;
  private File outputDir;
  private File packageOuputDir;
  private File pluginFile;
  private File homeDir;
  private File unpackedIncludeDir;
  
  private URL[] classpath;
  private final URLClassLoader classloader;
  private ClasspathPackageLoader packageLoader;
  
  private Collection<Artifact> pluginDependencies;
  
  private List<String> defaultPackages = Collections.emptyList();

  public MavenBuildContext(MavenProject project, Collection<Artifact> pluginDependencies, Log log) throws MojoExecutionException {
    this.project = project;
    this.logger = new MavenBuildLogger(log);
    
    this.buildDir = new File(project.getBuild().getDirectory());
    this.outputDir = new File(project.getBuild().getOutputDirectory());
    this.packageOuputDir = new File(project.getBuild().getOutputDirectory() + File.separator + 
        project.getGroupId().replace('.', File.separatorChar) + File.separator +  
        project.getArtifactId());
    this.pluginDependencies = pluginDependencies;
    this.homeDir = new File(buildDir, "gnur");
    this.pluginFile = new File(buildDir, "bridge.so");
    this.unpackedIncludeDir = new File(buildDir, "include");
    this.classpath = buildClassPath();

    ensureDirExists(outputDir);
    ensureDirExists(packageOuputDir);
    ensureDirExists(getGnuRHomeDir());
    ensureDirExists(unpackedIncludeDir);
    
    classloader = new URLClassLoader(classpath, getClass().getClassLoader());
    packageLoader = new ClasspathPackageLoader(classloader);
  }

  private File ensureDirExists(File dir) throws MojoExecutionException {
    if(dir.exists()) {
      if(!dir.isDirectory()) {
        throw new MojoExecutionException(dir.getAbsoluteFile() + " is not a directory.");
      }
    } else {
      boolean created = dir.mkdirs();
      if(!created) {
        throw new MojoExecutionException("Failed to create directory " + dir.getAbsolutePath());
      }
    }
    return dir;
  }

  public void setupNativeCompilation()  {
    // Unpack any headers from dependencies
    try {
      GccBridgeHelper.unpackHeaders(logger.getLog(), unpackedIncludeDir, project.getCompileArtifacts());
    } catch (MojoExecutionException e) {
      throw new RuntimeException(e);
    }
    try {
      GnurInstallation.unpackRHome(homeDir);
    } catch (IOException e) {
      throw new BuildException("Failed to unpack GNU R installation", e);
    }
    try {
      Gcc.extractPluginTo(pluginFile);
    } catch (IOException e) {
      throw new BuildException("Failed to unpack GCC Bridge Plugin", e);
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
  public File getCompileLogDir() {
    return new File(buildDir, "gcc-bridge-logs");
  }

  @Override
  public File getPackageOutputDir() {
    return packageOuputDir;
  }

  @Override
  public PackageLoader getPackageLoader() {
    return packageLoader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classloader;
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

  public void setDefaultPackages(List<String> defaultPackages) {
    if(defaultPackages == null) {
      this.defaultPackages = Collections.emptyList();
    } else {
      this.defaultPackages = defaultPackages;
    }
  }

  @Override
  public List<String> getDefaultPackages() {
    return defaultPackages;
  }

  public void setDefaultPackagesIfDependencies() {
    List<String> defaultPackages = new ArrayList<>();
    for (String packageName : DefaultPackages.DEFAULT_PACKAGES) {
      if(isDependency(packageName)) {
        defaultPackages.add(packageName);
      }
    }
    setDefaultPackages(defaultPackages);
  }

  private boolean isDependency(String packageName) {
    Iterable<Artifact> dependencies = project.getCompileArtifacts();
    for (Artifact dependency : dependencies) {
      if(dependency.getGroupId().equals("org.renjin") &&
          dependency.getArtifactId().equals(packageName)) {
        return true;
      }
    }
    return false;
  }
}
