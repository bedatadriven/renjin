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
