package org.renjin.maven;

import com.google.common.collect.Lists;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.renjin.gnur.GnurSourcesCompiler;

import java.io.File;
import java.util.List;

/**
 * Compiles gnur C/Fortran sources to a JVM class
 * 
 * @goal gnur-sources-compile
 * @phase compile
 * @requiresProject true
 */
public class GnurCompilerMojo extends AbstractMojo {


  /**
   * @parameter expression="${project.groupId}"
   * @required
   */
  private String groupId;
  
  /**
   * @parameter expression="${project.artifactId}"
   * @required
   */
  private String artifactId;
  
  /**
   * @parameter default-value="${plugin.artifacts}"
   * @readonly
   * @since 1.1-beta-1
   */
  private List<Artifact> pluginDependencies;
  
  /**
   * @parameter default-value="${project.basedir}"
   * @readonly
   */
  private File baseDir;
  
  /**
   * Name of the R package
   * @parameter expression="${project.build.outputDirectory}"
   * @required
   * @readonly
   */
  private File outputDirectory; 
  
  /**
   * Directory to which the intermediate jimple files are written
   * @parameter expression="${project.build.directory}/jimple"
   * @required
   */
  private File jimpleDirectory;

  /**
   * Directory to which the intermediate gimple files are written
   * @parameter expression="${project.build.directory}/gimple"
   * @required
   */
  private File gimpleDirectory;

  /**
   * Directories in which to look for C/Fortran sources
   * @parameter
   */
  private List<File> sourceDirectories;

  /**
   * @parameter expression="${ignore.gnur.compilation.failure}" default-value="false"
   */
  private boolean ignoreFailure;

  /**
   * Scratch directory for GCC output/files
   * @parameter expression="${project.build.directory}/gcc-work
   */
  private File workDirectory;


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
    
    try {
      compiler.compile();
    } catch (Exception e) {
      if(ignoreFailure) {
        System.err.println("Compilation of GNU R sources failed");
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
