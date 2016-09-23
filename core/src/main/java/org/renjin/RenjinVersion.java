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
package org.renjin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RenjinVersion {

  public static String getVersionName() {

    String propFileName = "Renjin.version.properties";
    Properties prop = new Properties();

    InputStream in = RenjinVersion.class.getResourceAsStream(propFileName);
    if (in == null) {
      throw new AssertionError("property file '" + propFileName + "' not found in the classpath");
    }
    try {
      prop.load(in);
    } catch (IOException e) {
      throw new AssertionError("Failed to load " + propFileName, e);
    }

    return prop.getProperty("renjin.display.version");
  }


}
