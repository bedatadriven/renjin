package org.renjin.packaging;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.renjin.packaging.PackageDescription.parseFileList;

public class PackageDescriptionTest {

  @Test
  public void parseCollateTest() {
    // From df2json
    assertThat(parseFileList("'df2json-package.r' 'df2json.R'"), contains("df2json-package.r", "df2json.R"));
    
    // Simple case
    assertThat(parseFileList("a.R b.R"), contains("a.R", "b.R"));

    // Double quotes
    assertThat(parseFileList("\"a file.R\" \"b.R\""), contains("a file.R", "b.R"));

  }
  
}