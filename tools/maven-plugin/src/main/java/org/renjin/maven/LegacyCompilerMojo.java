package org.renjin.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.renjin.legacy.LegacySourcesCompiler;

import com.google.common.collect.Lists;

/**
 * Compiles legacy C/Fortran sources to a JVM class
 * 
 * @goal legacy-sources-compile
 * @phase compile
 * @requiresProject true
 */
public class LegacyCompilerMojo extends AbstractMojo {


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
  
  
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    LegacySourcesCompiler compiler = new LegacySourcesCompiler();
    compiler.addSources(sourceDir("c"));
    compiler.setVerbose(false);
    compiler.setPackageName(groupId + "." + artifactId);
    compiler.setClassName(properCase(artifactId));
    compiler.addClassPaths(pluginDependencies());
    compiler.setOutputDirectory(outputDirectory);
    compiler.setJimpleDirectory(jimpleDirectory);
    
    try {
      compiler.compile();
    } catch (Exception e) {
      throw new MojoExecutionException("Compilation of legacy sources failed", e);
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

  private String properCase(String id) {
    return id.substring(0,1).toUpperCase() + id.substring(1);
  }

}
