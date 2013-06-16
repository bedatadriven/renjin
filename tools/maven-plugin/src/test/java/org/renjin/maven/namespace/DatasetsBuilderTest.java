package org.renjin.maven.namespace;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

public class DatasetsBuilderTest extends TestCase {

  
  public void testDatasets() throws FileNotFoundException {
    
    DatasetsBuilder builder = new DatasetsBuilder(
        new File("target/test-classes"),
        new File("src/test/resources/data"));
    builder.build();
  }
}
