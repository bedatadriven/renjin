package org.renjin.maven;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.renjin.gnur.GnurSourcesCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Compiles gnur C/Fortran sources to a JVM class

 */
@Mojo(name = "gnur-sources-compile", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurCompilerMojo extends AbstractMojo {
  
  @Component
  private MavenProject project;

  @Component
  private MavenProjectHelper projectHelper;

  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
  private List<Artifact> pluginDependencies;
  
  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;

  /**
   * Directory to which the intermediate gimple files are written
   */
  @Parameter(defaultValue = "${project.build.directory}/gimple", required = true)
  private File gimpleDirectory;

  /**
   * Directories in which to look for C/Fortran sources
   */
  @Parameter
  private List<File> sourceDirectories;

  /**
   * If true, do not fail the build if compilation fails.
   */
  @Parameter(property = "ignore.gnur.compilation.failure", defaultValue = "false")
  private boolean ignoreFailure;

  /**
   * Scratch directory for GCC output/files
   */
  @Parameter(defaultValue = "${project.build.directory}/gcc-work")
  private File workDirectory;

  @Parameter
  private List<File> includeDirectories;

  @Parameter(defaultValue = "${project.build.finalName}")
  private String finalName;

  @Parameter(defaultValue = "${project.build.directory}/include")
  private File unpackedIncludeDir;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    if (sourceDirectories == null || sourceDirectories.isEmpty()) {
      sourceDirectories = Lists.newArrayList(sourceDir("c"), sourceDir("fortran"));
    }

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    for (File sourceDir : sourceDirectories) {
      compiler.addSources(sourceDir);
    }

    workDirectory.mkdirs();
    gimpleDirectory.mkdirs();
    outputDirectory.mkdirs();

    compiler.setVerbose(false);
    compiler.setPackageName(project.getGroupId() + "." + project.getArtifactId());
    compiler.setClassName(project.getArtifactId());
    compiler.setWorkDirectory(workDirectory);
    compiler.setOutputDirectory(outputDirectory);
    compiler.setGimpleDirectory(gimpleDirectory);
    compiler.setLinkClassLoader(getLinkClassLoader());
    
    // Unpack any headers from dependencies
    unpackHeaders();
    compiler.addIncludeDir(unpackedIncludeDir);

    // Add the standard GNU R inst/include to the compiler's
    // include path if it exists
    File instDir = new File(project.getBasedir(), "inst");
    File instIncludeDir = new File(instDir, "include");
    if (instIncludeDir.exists()) {
      getLog().info("Adding " + instIncludeDir.getAbsolutePath() + " to include path...");
      compiler.addIncludeDir(instIncludeDir);
    }

    if (includeDirectories != null) {
      for (File includeDirectory : includeDirectories) {
        compiler.addIncludeDir(includeDirectory);
      }
    }

    try {
      compiler.compile();
      getLog().info("Compilation of GNU R sources succeeded.");
    } catch (Exception e) {
      if (ignoreFailure) {
        getLog().error("Compilation of GNU R sources failed.");
        e.printStackTrace(System.err);
      } else {
        throw new MojoExecutionException("Compilation of GNU R sources failed", e);
      }
    }

    if (instIncludeDir.exists()) {
      archiveHeaders(instIncludeDir);
    }
  }

  private File sourceDir(String subDirectory) {
    return new File(project.getBasedir().getAbsolutePath() + 
        File.separator + "src" + 
        File.separator + "main" +
        File.separator + subDirectory);
  }

  private List<File> pluginDependencies() {
    List<File> paths = Lists.newArrayList();
    for (Artifact artifact : pluginDependencies) {
      paths.add(artifact.getFile());
    }
    return paths;
  }
  
  private void unpackHeaders() throws MojoExecutionException {

    ensureDirExists(unpackedIncludeDir);

    List<Artifact> compileArtifacts = project.getCompileArtifacts();
    for (Artifact compileArtifact : compileArtifacts) {
      if("headers".equals(compileArtifact.getClassifier())) {
        extractHeaders(compileArtifact);
      }
    }
  }


  public void archiveHeaders(File includeDirectory) throws MojoExecutionException, MojoFailureException {

    File outputDir = new File(project.getBuild().getDirectory());
    File archiveFile = new File(outputDir, finalName + "-headers.jar");

    getLog().debug("Archiving headers in " + includeDirectory.getAbsolutePath());

    try (JarOutputStream output = new JarOutputStream(new FileOutputStream(archiveFile))) {
      archiveFiles(output, includeDirectory, "");
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to create headers archive", e);
    }

    projectHelper.attachArtifact(project, "jar", "headers", archiveFile);
  }

  private void archiveFiles(JarOutputStream output, File dir, String prefix) throws IOException {
    File[] includeFiles = dir.listFiles();
    if (includeFiles != null) {
      for (File includeFile : includeFiles) {
        if (includeFile.isDirectory()) {
          archiveFiles(output, includeFile, prefix + includeFile.getName() + "/");
        } else {
          JarEntry entry = new JarEntry(prefix + includeFile.getName());
          output.putNextEntry(entry);
          Files.asByteSource(includeFile).copyTo(output);
        }
      }
    }
  }

  private void extractHeaders(Artifact compileArtifact) throws MojoExecutionException {
    if(compileArtifact.getFile() == null) {
      throw new MojoExecutionException("Depedency " + compileArtifact.getId() + " has not been resolved.");
    }
    try(JarInputStream in = new JarInputStream(new FileInputStream(compileArtifact.getFile()))) {
      JarEntry entry;
      while((entry=in.getNextJarEntry()) != null) {
        if(!entry.isDirectory()) {
          File destFile = new File(unpackedIncludeDir + File.separator + entry.getName());

          getLog().debug("Unpacking " + entry.getName() + " to " + destFile);

          ensureDirExists(destFile.getParentFile());
          Files.asByteSink(destFile).writeFrom(in);
        }
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Exception unpacking headers", e);
    }
  }

  private void ensureDirExists(File unpackDir) throws MojoExecutionException {
    if(!unpackDir.exists()) {
      boolean created = unpackDir.mkdirs();
      if(!created) {
        throw new MojoExecutionException("Failed to create include directory " + unpackDir.getAbsolutePath());
      }
    }
  }

  private ClassLoader getLinkClassLoader() throws MojoExecutionException  {
    try {
      getLog().debug("GCC-Bridge Link Classpath: ");

      List<URL> classpathURLs = Lists.newArrayList();
      classpathURLs.add( new File(project.getBuild().getOutputDirectory()).toURI().toURL() );

      for(Artifact artifact : (List<Artifact>)project.getCompileArtifacts()) {
        getLog().debug("  "  + artifact.getFile());

        classpathURLs.add(artifact.getFile().toURI().toURL());
      }

      return new URLClassLoader( classpathURLs.toArray( new URL[ classpathURLs.size() ] ), getClass().getClassLoader());
    } catch(MalformedURLException e) {
      throw new MojoExecutionException("Exception resolving classpath", e);
    }
  }
}