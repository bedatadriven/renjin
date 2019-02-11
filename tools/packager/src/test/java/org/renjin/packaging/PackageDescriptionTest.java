/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.packaging;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
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

  @Test
  public void renjinImportTest() {
    PackageDescription.Dependency cranDependency = new PackageDescription.Dependency("org.renjin.cran:readxl");
    assertThat(cranDependency.getName(), equalTo("readxl"));
    assertThat(cranDependency.getGroupId(), equalTo("org.renjin.cran"));

  }
  
}