package org.renjin.gcc;

import java.net.URL;
import java.net.URLClassLoader;

public class GccClassloader extends URLClassLoader {

  public GccClassloader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public void addURL(URL url) {
    super.addURL(url);
  }

}
