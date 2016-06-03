package org.renjin.gcc.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Archives headers into an adjacent "headers" jar
 */
@Mojo(name = "archive-headers",  requiresDependencyCollection = ResolutionScope.COMPILE)
public class ArchiveHeadersMojo extends AbstractMojo {
  
  
  @Parameter
  private File includeDirectory;
  
  @Component
  private MavenProject project;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    GccBridgeHelper.archiveHeaders(getLog(), project, includeDirectory);
  }
}
