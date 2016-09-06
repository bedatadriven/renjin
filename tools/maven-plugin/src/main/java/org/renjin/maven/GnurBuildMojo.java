package org.renjin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.gcc.maven.GccBridgeHelper;
import org.renjin.packaging.PackageBuilder;
import org.renjin.packaging.PackageSource;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.Collections;

/**
 * Builds a complete package laid out according to GNU R conventions.
 */
@ThreadSafe
@Mojo(name = "gnur-compile", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurBuildMojo extends AbstractMojo {


  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /**
   * If true, do not fail the build if compilation fails.
   */
  @Parameter(property = "ignore.gnur.compilation.failure", defaultValue = "false")
  private boolean ignoreFailure;


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    try {
      PackageSource packageSource = new PackageSource.Builder(project.getBasedir())
          .setGroupId(project.getGroupId())
          .build();
      
      MavenBuildContext buildContext = new MavenBuildContext(project, Collections.<Artifact>emptySet(), getLog());
      buildContext.setDefaultPackagesIfDependencies();
      
      PackageBuilder builder = new PackageBuilder(packageSource, buildContext);
      builder.build();

      archiveHeaders(buildContext);


    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    
  }


  private void archiveHeaders(MavenBuildContext buildContext) throws MojoExecutionException {

    File stagingIncludes = new File(buildContext.getPackageOutputDir(), "include");
    
    if(stagingIncludes.exists()) {
      GccBridgeHelper.archiveHeaders(getLog(), project, stagingIncludes);
    }
  }
  
}
