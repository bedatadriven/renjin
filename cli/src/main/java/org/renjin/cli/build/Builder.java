/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.cli.build;

import org.renjin.packaging.BuildReporter;
import org.renjin.packaging.PackageSource;
import org.renjin.repackaged.guava.base.Strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Builds an R package
 */
public class Builder {

  public static void execute(String action, String[] args) throws IOException {
    BuildReporter reporter = new BuildReporter();

    List<String> packagePaths = Arrays.asList(args);
    for (String packagePath : packagePaths) {
      PackageSource source = new PackageSource.Builder(packagePath)
          .setGroupId("org.renjin.cran")
          .build();
      
      PackageBuild build = new PackageBuild(reporter, source, buildSuffix());
      
      if(action.equals("build") || action.equals("install")) {
        build.build();
      }
      if(action.equals("install")) {
        build.install();
      }
    }
  }

  private static String buildSuffix() {
    String envBuildNum = Strings.nullToEmpty(System.getProperty("BUILD_NUMBER"));
    if(envBuildNum.isEmpty()) {
      envBuildNum = Strings.nullToEmpty(System.getenv("BUILD_NUMBER"));
    }
    
    if(envBuildNum.isEmpty()) {
      return "";
    } else {
      return "-b" + envBuildNum;
    }
  }
}
