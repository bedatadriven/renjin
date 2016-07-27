package org.renjin.gcc.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Soot optimizer on one or more classfiles
 */
@ThreadSafe
@Mojo(name = "soot",  requiresDependencyCollection = ResolutionScope.COMPILE)
public class SootMojo extends AbstractMojo {

  @Component
  private MavenProject project;
  
  @Parameter
  private List<String> optimize;

  @Parameter
  private boolean verbose = false;
  
  @Parameter
  private boolean debug = false;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    
    if(optimize == null || optimize.isEmpty()) {
      throw new MojoFailureException("No classes to optimize.");
    }
    
    List<String> args = new ArrayList<>();
    // Preprend maven's classpath
    args.add("-pp");
    
    // Classpath for soot analysis
    args.add("-cp");
    args.add(compileClassPath());
    
    if(verbose) {
      args.add("-v");
    }
    
    if(debug) {
      args.add("-debug");
    }
    
    // Add classes to optimize
    args.add("-O");
    args.addAll(optimize);
    
    // Write out to build directory and overwrite existing classfiles
    args.add("-d");
    args.add(project.getBuild().getOutputDirectory());
    
    soot.Main.main(args.toArray(new String[args.size()]));
  }

  private String compileClassPath() throws MojoExecutionException {

    List<String> compileClasspathElements;
    try {
      compileClasspathElements = project.getCompileClasspathElements();
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Failed to resolve classpath", e);
    }
    getLog().debug("Soot Classpath: ");

    StringBuilder classpath = new StringBuilder();
    classpath.append(project.getBuild().getOutputDirectory());
    for(String element : compileClasspathElements) {
      getLog().debug("  "  + element);
      classpath.append(File.pathSeparator);
      classpath.append(element);
    }
    return classpath.toString();
  }

}
