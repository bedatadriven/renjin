package org.renjin.maven;

import com.google.common.collect.Lists;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.renjin.gnur.GnurSourcesCompiler;

import java.io.File;
import java.util.List;

/**
 * Compiles gnur C/Fortran sources to a JVM class

 */
@Mojo(name = "gnur-sources-compile", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurCompilerMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.groupId}")
  private String groupId;
  
  @Parameter(defaultValue = "${project.artifactId}", required = true)
  private String artifactId;
  
  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
  private List<Artifact> pluginDependencies;
  
  @Parameter(defaultValue = "${project.basedir}", readonly = true)
  private File baseDir;
  
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


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    if(sourceDirectories == null || sourceDirectories.isEmpty()) {
      sourceDirectories = Lists.newArrayList(sourceDir("c"), sourceDir("fortran"));
    }

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    for(File sourceDir : sourceDirectories) {
      compiler.addSources(sourceDir);
    }

    workDirectory.mkdirs();
    gimpleDirectory.mkdirs();
    outputDirectory.mkdirs();

    compiler.setVerbose(false);
    compiler.setPackageName(groupId + "." + artifactId);
    compiler.setClassName(artifactId);
    compiler.addClassPaths(pluginDependencies());
    compiler.setWorkDirectory(workDirectory);
    compiler.setOutputDirectory(outputDirectory);
    compiler.setGimpleDirectory(gimpleDirectory);
    
    if(includeDirectories != null) {
      for (File includeDirectory : includeDirectories) {
        compiler.addIncludeDir(includeDirectory);
      }
    }
    
    try {
      compiler.compile();
      getLog().info("Compilation of GNU R sources succeeded.");
    } catch (Exception e) {
      if(ignoreFailure) {
        getLog().error("Compilation of GNU R sources failed.");
        e.printStackTrace(System.err);
      } else {
        throw new MojoExecutionException("Compilation of GNU R sources failed", e);
      }
    }
  }

  private File sourceDir(String subDirectory) {
    return new File(baseDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + 
          File.separator + subDirectory);
  }

  private List<File> pluginDependencies() {
    List<File> paths = Lists.newArrayList();
    for(Artifact artifact : pluginDependencies) {
      paths.add(artifact.getFile());
    }
    return paths;
  }
}
