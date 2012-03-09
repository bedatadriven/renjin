package org.renjin.packaging;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class Packager {

  private File packageRoot;
  private File outDir;
  
  public Packager(File packageRoot, File outDir) {
    this.packageRoot = packageRoot;
    this.outDir = outDir;
  }
  
  public void doPackage() throws IOException {
    File namespaceFile = new File(packageRoot, "NAMESPACE");
    if(namespaceFile.exists()) {
      Files.copy(namespaceFile, new File(outDir, "NAMESPACE"));
    }
    File sourceDescriptionFile = new File(packageRoot, "DESCRIPTION");
    File targetDescriptionFile = new File(outDir, "DESCRIPTION");
    Files.copy(sourceDescriptionFile, targetDescriptionFile);
    Files.append("Built: R 2.10); ; " + new Date() + "; Java", targetDescriptionFile,
        Charsets.UTF_8);
  }
  
  public static void main(String[] args) {
    
    
  }
  
}
