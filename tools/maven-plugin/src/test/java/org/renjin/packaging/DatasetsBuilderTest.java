package org.renjin.packaging;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;

public class DatasetsBuilderTest extends TestCase {

  
  public void testDatasets() throws FileNotFoundException {
    
    DatasetsBuilder builder = new DatasetsBuilder(
        new File("src/test/resources/data"),
        new File("target/test-classes"));
    builder.build();
  }
  
}
