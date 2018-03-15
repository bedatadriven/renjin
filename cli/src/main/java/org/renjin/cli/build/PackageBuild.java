/*
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
package org.renjin.cli.build;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.renjin.aether.AetherFactory;
import org.renjin.eval.Session;
import org.renjin.gnur.GnurSourcesCompiler;
import org.renjin.packaging.*;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.Files;

import java.io.*;
import java.util.Properties;


public class PackageBuild {
  
  private BuildReporter reporter;

  private final PackageSource source;
  private final String buildVersion;

  /**
   * The directory to write files to be included in the jar 
   */
  private final File stagingDir;

  private final File buildDir;

  /**
   * Directory for GCC work
   */
  private final File gccWorkDir;
  
  /**
   * The subdir within the stagingDir corresponding to this package's java package directory.
   * For example, $stagingDir/org/renjin/bioconductor/limma
   */
  private final File outputDir;
  
  private final File mavenMetaDir;

  private final File environmentFile;

  public PackageBuild(BuildReporter reporter, PackageSource source, String buildSuffix) {
    this.reporter = reporter;
    this.source = source;
    this.buildVersion = source.getVersion() + buildSuffix;
    this.buildDir = createCleanBuildDir(source.getPackageDir());
    this.stagingDir = new File(buildDir, "classes");
    this.gccWorkDir = new File(buildDir, "gcc-work");
    this.outputDir = new File(
            stagingDir + File.separator + 
            source.getGroupId().replace('.', File.separatorChar) + File.separator + 
            source.getPackageName());
    
    mkdirs(outputDir);

    this.mavenMetaDir = new File(
            stagingDir + File.separator + 
            "META-INF" + File.separator + 
            "maven" + File.separator +
            source.getGroupId() + File.separator + 
            source.getPackageName());
    
    mkdirs(mavenMetaDir);
    
    environmentFile = new File(outputDir, "environment");  
  }

  private static File createCleanBuildDir(File packageDir) {
    File buildDir = new File(packageDir, "build");
    if(buildDir.exists()) {
      try {
        FileUtils.deleteDirectory(buildDir);
      } catch (IOException e) {
        throw new BuildException("Failed to delete build dir", e);
      }
    }
    boolean created = buildDir.mkdirs();
    if(!created) {
      throw new BuildException("Failed to create build dir");
    }
    return buildDir;
  }

  private void mkdirs(File dir) {
    if(!dir.exists()) {
      boolean created = dir.mkdirs();
      if(!created) {
        throw new BuildException("Failed to create " + dir.getAbsolutePath());
      }
    }
  }


  /**
   * @return the temporary directory where files to be included in the package's JAR will be written.
   */
  public File getStagingDir() {
    return stagingDir;
  }

  public File getEnvironmentFile() {
    return environmentFile;
  }

  public File getJarFile() {
    return new File(source.getPackageDir().getParentFile(), source.getPackageName() + "-" + buildVersion + ".jar");
  }

  public File getPomFile() {
    return new File(mavenMetaDir, "pom.xml");
  }
  
  public File getPomPropertiesFile() {
    return new File(mavenMetaDir, "pom.properties");
  }
  
  public void build() {
    if(source.needsCompilation()) {
      compileNativeSources();
    }
    compileDatasets();
    copyResources();
    compileNamespace();
    runTests();
    writePomFile();
    writePomProperties();
    archivePackage();
  }

  private void compileDatasets() {
    DatasetsBuilder datasetsBuilder = new DatasetsBuilder(source.getDataDir(), outputDir);
    try {
      datasetsBuilder.build();
    } catch (FileNotFoundException e) {
      throw new BuildException("Exception compiling datasets", e);
    }
  }
  
  private void compileNativeSources() {
    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    compiler.addSources(source.getNativeSourceDir());
    compiler.setVerbose(false);
    compiler.setPackageName(source.getJavaPackageName());
    compiler.setClassName(source.getPackageName());
    compiler.setWorkDirectory(gccWorkDir("work"));
    compiler.setGimpleDirectory(gccWorkDir("gimple"));
    compiler.setOutputDirectory(stagingDir);
    compiler.setLoggingDir(gccWorkDir("logging"));

    try {
      compiler.compile();
    } catch (Exception e) {
      throw new RuntimeException(e);
     // reporter.warn("Compilation of GNU R sources failed", e);
    }
  }

  private File gccWorkDir(String subDir) {
    File dir = new File(gccWorkDir, subDir);
    boolean created = dir.mkdirs();
    if(!created) {
      throw new BuildException(String.format("Failed to create working directory '%s'", dir.getAbsolutePath()));
    }
    return dir;
  }

  /**
   * Copies files that should be available to installed packages
   */
  private void copyResources()  {
    try {
      Files.copy(source.getNamespaceFile(), new File(outputDir, "NAMESPACE"));
      source.getDescription().writeTo(new File(outputDir, "DESCRIPTION"));
      
    } catch (IOException e) {
      throw new BuildException("Exception copying package resources");
    }
  }

  private void compileNamespace() {
    
    NamespaceBuilder builder = new NamespaceBuilder();
    try {
      builder.build(source.getGroupId(), source.getPackageName(), source.getNamespaceFile(),
          source.getSourceFiles(),
          environmentFile,
          Session.DEFAULT_PACKAGES);
      

    } catch (IOException e) {
      throw new BuildException("Exception building namespace: " + e.getMessage(), e);
    }
  }
  
  private void runTests() {
    TestRun run = new TestRun(stagingDir, source.getTestsDir());
    run.execute();
  }
  
  private void writePomFile() {
    PomBuilder builder = new PomBuilder(source, buildVersion);
    try {
      Files.write(builder.getXml(), getPomFile(), Charsets.UTF_8);
    } catch (IOException e) {
      throw new BuildException("Exception writing "   + getPomFile().getAbsolutePath());
    }
  }
  
  private void writePomProperties() {
    Properties properties = new Properties();
    properties.setProperty("groupId", source.getGroupId());
    properties.setProperty("artifactId", source.getPackageName());
    properties.setProperty("version", buildVersion);

    try {
      OutputStream out = new FileOutputStream(getPomPropertiesFile());
      properties.store(out, "Generated by Renjin");
    } catch (IOException e) {
      throw new BuildException("Exception writing " + getPomPropertiesFile().getAbsolutePath());
    }
  }

  private void archivePackage() {
    try(JarArchiver archiver = new JarArchiver(getJarFile())) {
      archiver.addDirectory(stagingDir);
    } catch (Exception e) {
      throw new BuildException("Failed to create package jar", e);
    }
  }


  public void install() {
    RepositorySystem system = AetherFactory.newRepositorySystem();
    RepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);

    Artifact jarArtifact = new DefaultArtifact( source.getGroupId(), source.getPackageName(), "jar", buildVersion);
    jarArtifact = jarArtifact.setFile(getJarFile());

    Artifact pomArtifact = new SubArtifact( jarArtifact, "", "pom" );
    pomArtifact = pomArtifact.setFile( new File( "pom.xml" ) );

    InstallRequest installRequest = new InstallRequest();
    installRequest.addArtifact( jarArtifact ).addArtifact( pomArtifact );

    try {
      system.install( session, installRequest );
    } catch (InstallationException e) {
      throw new BuildException("Exception installing artifact " + getJarFile().getAbsolutePath());
    }
  }
}
