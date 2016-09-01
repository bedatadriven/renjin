package org.renjin.packaging;


import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;

public class PackageBuilder {
  
  private PackageSource packageSource;
  private BuildContext context;

  private boolean ignoreNativeCompilationFailure;
  
  public PackageBuilder(PackageSource packageSource, BuildContext context) {
    this.packageSource = packageSource;
    this.context = context;
  }

  public void setIgnoreNativeCompilationFailure(boolean ignoreNativeCompilationFailure) {
    this.ignoreNativeCompilationFailure = ignoreNativeCompilationFailure;
  }

  public void build() throws IOException {
    copyRootFiles();
    compileNativeSources();
    copyInstalledFiles();
  }


  /**
   * Copies files from the package root, including DESCRIPTION and NAMESPACE
   */
  private void copyRootFiles() throws IOException {
    copyRootFile(packageSource.getDescriptionFile());
    copyRootFile(packageSource.getNamespaceFile());
  }

  private void copyRootFile(File file) throws IOException {
    Files.copy(file, new File(context.getPackageOutputDir(), file.getName()));
  }

  private void compileNativeSources() {

    if(packageSource.getNativeSourceDir().exists()) {
      context.setupNativeCompilation();

      try {
        NativeSourceBuilder nativeSourceBuilder = new NativeSourceBuilder(packageSource, context);
        nativeSourceBuilder.build();
      } catch (Exception e) {
        if (ignoreNativeCompilationFailure) {
          context.getLogger().error("Compilation of GNU R sources failed.");
          e.printStackTrace(System.err);
        } else {
          throw new BuildException("Compilation of GNU R sources failed", e);
        }
      }
    }
  }

  /**
   * Copies files recursively from the inst/ directory to the packageOutputDir
   */
  private void copyInstalledFiles() throws IOException {

    if(packageSource.getInstalledFilesDir().exists()) {
      copyInstalledFiles(packageSource.getInstalledFilesDir(), context.getPackageOutputDir());      
    }
  }

  private void copyInstalledFiles(File sourceDir, File targetDir) throws IOException {
    
    if(!targetDir.exists()) {
      boolean created = targetDir.mkdirs();
      if(!created) {
        throw new IOException("Failed to create output directory '" + targetDir.getAbsolutePath() + "'");
      }
    }
    
    File[] sourceFiles = sourceDir.listFiles();
    if(sourceFiles != null) {
      for (File sourceFile : sourceFiles) {
        File targetFile = new File(targetDir, sourceFile.getName());
        if(sourceFile.isDirectory()) {
          copyInstalledFiles(sourceFile, targetFile);
        } else {
          Files.copy(sourceFile, targetFile);
        }
      }
    }
  }

}
