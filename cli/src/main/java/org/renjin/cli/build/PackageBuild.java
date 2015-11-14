package org.renjin.cli.build;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
import org.renjin.packaging.DatasetsBuilder;
import org.renjin.packaging.NamespaceBuilder;

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
            source.getName());
    
    mkdirs(outputDir);

    this.mavenMetaDir = new File(
            stagingDir + File.separator + 
            "META-INF" + File.separator + 
            "maven" + File.separator +
            source.getGroupId() + File.separator + 
            source.getName());
    
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
    return new File(source.getPackageDir().getParentFile(), source.getName() + "-" + buildVersion + ".jar");
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
    compiler.setClassName(source.getName());
    compiler.setWorkDirectory(gccWorkDir("work"));
    compiler.setGimpleDirectory(gccWorkDir("gimple"));
    compiler.setOutputDirectory(stagingDir);

    try {
      compiler.compile();
    } catch (Exception e) {
      throw new RuntimeException(e);
      //reporter.warn("Compilation of GNU R sources failed", e);
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

  private void compileNamespace() {
    NamespaceBuilder builder = new NamespaceBuilder();
    try {
      builder.build(source.getGroupId(), source.getName(), source.getNamespaceFile(),
          source.getSourceDir(),
          source.getSourceFiles(),
          environmentFile,
          Session.DEFAULT_PACKAGES);
      
      Files.copy(source.getNamespaceFile(), new File(outputDir, "NAMESPACE"));

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
      throw new BuildException("Exception writing " + getPomFile().getAbsolutePath());
    }
  }
  
  private void writePomProperties() {
    Properties properties = new Properties();
    properties.setProperty("groupId", source.getGroupId());
    properties.setProperty("artifactId", source.getName());
    properties.setProperty("version", buildVersion);

    try {
      OutputStream out = new FileOutputStream(getPomPropertiesFile());
      properties.store(out, "Generated by Renjin");
    } catch (IOException e) {
      throw new BuildException("Exception writing " + getPomPropertiesFile().getAbsolutePath());
    }
  }

  private void archivePackage() {
    try {
      JarArchiver archiver = new JarArchiver(getJarFile());
      archiver.addDirectory(stagingDir);
      archiver.close();
    } catch (Exception e) {
      throw new BuildException("Failed to create package jar", e);
    }
  }


  public void install() {
    RepositorySystem system = AetherFactory.newRepositorySystem();
    RepositorySystemSession session = AetherFactory.newRepositorySystemSession(system);

    Artifact jarArtifact = new DefaultArtifact( source.getGroupId(), source.getName(), "jar", buildVersion);
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
