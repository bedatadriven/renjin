package org.renjin.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.packaging.NativeSourceBuilder;
import org.renjin.packaging.PackageSource;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * Builds a complete package laid out according to GNU R conventions.
 */
@ThreadSafe
@Mojo(name = "build-gnur", requiresDependencyCollection = ResolutionScope.COMPILE)
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

    PackageSource packageSource;
    try {
      packageSource = new PackageSource.Builder(project.getBasedir())
          .setGroupId(project.getGroupId())
          .build();
    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

    MavenBuildContext buildContext = new MavenBuildContext(project);
    compileNativeSources(packageSource, buildContext);

    

    throw new UnsupportedOperationException();
  }

  private void compileNativeSources(PackageSource packageSource, MavenBuildContext buildContext) 
      throws MojoExecutionException {
    
    if(packageSource.getNativeSourceDir().exists()) {
      buildContext.setupNativeCompilation();

      try {
        NativeSourceBuilder nativeSourceBuilder = new NativeSourceBuilder(packageSource, buildContext);
        nativeSourceBuilder.build();
      } catch (Exception e) {
        if (ignoreFailure) {
          getLog().error("Compilation of GNU R sources failed.");
          e.printStackTrace(System.err);
        } else {
          throw new MojoExecutionException("Compilation of GNU R sources failed", e);
        }
      }
    }
  }
}
