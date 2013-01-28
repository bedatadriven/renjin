package org.renjin.primitives.packaging;

import java.io.IOException;

import org.junit.Test;
import org.renjin.sexp.SEXP;

public class ClasspathPackageTest {

  @Test
  public void loadDataset() throws IOException {
    
    ClasspathPackage pkg = new ClasspathPackage("org.renjin.cran", "survival");
    SEXP df = pkg.loadDataset("bladder");  
    System.out.println(df.getTypeName());
  
  }
  
  @Test
  public void lzma() throws IOException {
    ClasspathPackage pkg = new ClasspathPackage("org.renjin.cran", "survival");

    SEXP df = pkg.loadDataset("nwtco");
    System.out.println(df.getTypeName());
    
  }
}
