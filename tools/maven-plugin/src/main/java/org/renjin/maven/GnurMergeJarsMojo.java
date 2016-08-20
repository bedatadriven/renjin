package org.renjin.maven;

import com.google.common.io.ByteStreams;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Merges one ore more jars included in a package source into the output.
 * 
 * <p>Packages based on rJava typically include java dependencies as Jars in the 
 * {@code inst/java} folder. This mojo unpacks all files from these jars and copies
 * them into the target/classes directory. In this way, they will be available on the 
 * package's classpath at runtime.</p>
 */
@ThreadSafe
@Mojo(name = "merge-gnur-jars", requiresDependencyCollection = ResolutionScope.COMPILE)
public class GnurMergeJarsMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.basedir}/inst/java")
  private File javaDir;

  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    File[] javaFiles = javaDir.listFiles();
    if(javaFiles != null) {
      for (File javaFile : javaFiles) {
        if(javaFile.getName().endsWith(".jar")) {
          getLog().info("Merging " + javaFile.getName());
          try {
            mergeJar(javaFile);
          } catch (IOException e) {
            throw new MojoExecutionException("Failed to merge " + javaFile.getName(), e);
          }
        }
      }
    }
  }

  private void mergeJar(File jarFile) throws IOException {
    try (JarInputStream in = new JarInputStream(new FileInputStream(jarFile))) {
      JarEntry entry;
      while ((entry = in.getNextJarEntry()) != null) {
        if (!entry.isDirectory()) {
          File outputFile = new File(outputDirectory.getAbsolutePath() + "/" + entry.getName());
          if(!outputFile.getParentFile().exists()) {
            boolean created = outputFile.getParentFile().mkdirs();
            if(!created) {
              throw new IOException("Failed to create " + outputFile.getParent());
            }
          }
          try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            ByteStreams.copy(in, outputStream);
          }
        }
      }
    }
  }
}
