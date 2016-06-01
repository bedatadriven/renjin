package org.renjin.maven;

import com.google.common.base.Joiner;
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
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.gnur.GnurSourcesCompiler;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a package written for GNU R
 */
@ThreadSafe
@Mojo(name = "make-gnur-sources", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurMakeMojo extends AbstractMojo {

  private static final List<String> SOURCE_EXTENSIONS = Lists.newArrayList("c", "f", "f77", "cpp", "cxx");


  @Parameter(defaultValue = "${project}", readonly = true)
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
  @Parameter(defaultValue = "${project.basedir}/src")
  private File nativeSourceDir;
  
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

  @Parameter(defaultValue = "${project.build.directory}/include", readonly = true)
  private File unpackedIncludeDir;

  @Parameter(defaultValue = "${project.build.directory}/gnur/include", readonly = true)
  private File rIncludeDir;
  
  @Parameter(defaultValue = "${project.build.directory}/gnur/Makefile", readonly = true)
  private File makefile;

  @Parameter(defaultValue = "${project.build.directory}/gnur/bridge.so", readonly = true)
  private File pluginFile;

  @Parameter(defaultValue = "${project.build.directory}/gcc-bridge-logs")
  private File logDir;
  

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      setupEnvironment();
      make();
      compileGimple();
      archiveHeaders();
      getLog().info("Compilation of GNU R sources succeeded.");

    } catch (InterruptedException e) {
      throw new MojoExecutionException("Interrupted");

    } catch (Exception e) {
      if (ignoreFailure) {
        getLog().error("Compilation of GNU R sources failed.");
        e.printStackTrace(System.err);
      } else {
        throw new MojoExecutionException("Compilation of GNU R sources failed", e);
      }
    } 
  }

  private void setupEnvironment() throws MojoExecutionException, IOException {
    // Unpack any headers from dependencies
    GccBridgeHelper.unpackHeaders(getLog(), unpackedIncludeDir, project.getCompileArtifacts());
    GnurCompilation.unpackIncludes(rIncludeDir);
    GnurCompilation.extractResource("Makefile", makefile);
    Gcc.extractPluginTo(pluginFile);
  }


  private void make() throws IOException, InterruptedException {
    
    getLog().info("PATH = " + System.getenv("PATH"));
    
    List<String> commandLine = Lists.newArrayList();
    commandLine.add("/usr/bin/make");

    // Combine R's default Makefile with package-specific Makevars if present
    File makevars = new File(nativeSourceDir, "Makevars");
    if (makevars.exists()) {
      commandLine.add("-f");
      commandLine.add("Makevars");
    }

    // Makeconf file
    commandLine.add("-f");
    commandLine.add(makefile.getAbsolutePath());

    commandLine.add("SHLIB='dummy.so'");
    commandLine.add("OBJECTS=" + findObjectFiles());
    commandLine.add("R_INCLUDE_DIR=" + rIncludeDir);
    commandLine.add("BRIDGE_PLUGIN=" + pluginFile);

    getLog().debug("Executing " + Joiner.on(" ").join(commandLine));

    // Execute...
    int exitCode = new ProcessBuilder()
        .command(commandLine)
        .directory(nativeSourceDir)
        .inheritIO().start().waitFor();
    if (exitCode != 0) {
      throw new InternalCompilerException("Failed to execute Makefile");
    }
  }


  private void compileGimple() throws MojoExecutionException {

    List<GimpleCompilationUnit> gimpleFiles = Lists.newArrayList();
    collectGimple(nativeSourceDir, gimpleFiles);
    
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setLoggingDirectory(logDir);
    compiler.setLinkClassLoader(GccBridgeHelper.getLinkClassLoader(project, getLog()));
    compiler.setOutputDirectory(outputDirectory);
    compiler.setPackageName(project.getGroupId() + "." + project.getArtifactId());
    compiler.setClassName(project.getArtifactId());

    try {
      GnurSourcesCompiler.setupCompiler(compiler);
    } catch (ClassNotFoundException e) {
      throw new MojoExecutionException("Failed to setup Gimple Compiler", e);
    }

    try {
      compiler.compile(gimpleFiles);
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to compile Gimple", e);
    }
  }

  private void archiveHeaders() throws MojoExecutionException {
    File instDir = new File(project.getBasedir(), "inst");
    File includeDir = new File(instDir, "include");
    
    if(includeDir.exists()) {
      GccBridgeHelper.archiveHeaders(project, includeDir);
    }
  }
  
  
  private String findObjectFiles() {
    List<String> objectFiles = new ArrayList<>();
    File[] files = nativeSourceDir.listFiles();
    if(files != null)  {
      for (File file : files) {
        String extension = Files.getFileExtension(file.getName());
        if(SOURCE_EXTENSIONS.contains(extension)) {
          String baseName = Files.getNameWithoutExtension(file.getName());
          objectFiles.add(baseName + ".o");
        }
      }
    }
    return Joiner.on(" ").join(objectFiles);
  }

  private void collectGimple(File dir, List<GimpleCompilationUnit> gimpleFiles) throws MojoExecutionException {
    
    GimpleParser parser = new GimpleParser();
    
    File[] files = dir.listFiles();
    if(files != null) {
      for (File file : files) {
        if(file.getName().endsWith(".gimple")) {
          try {
            gimpleFiles.add(parser.parse(file));
          } catch (IOException e) {
            throw new MojoExecutionException("Failed to parse gimple file " + file, e);
          }
        } else if(file.isDirectory()) {
          collectGimple(file, gimpleFiles);
        }
      }
    }
  }

}
