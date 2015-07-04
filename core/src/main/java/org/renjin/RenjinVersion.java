package org.renjin;

import java.io.*;
import java.util.Properties;

/**
 * Created by kallen on 10/10/14.
 */
public class RenjinVersion {

  public String getVersionName() throws IOException {

    String propFileName = "Renjin.version.properties";
    Properties prop = new Properties();

    InputStream in = getClass().getResourceAsStream(propFileName);
    if (in == null) {
      throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
    }
    prop.load(in);

    String version = prop.getProperty("renjin.display.version");

    return version;
  }


}
