package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

/**
 * Provides access to a Renjin package that 
 * is on the classpath.
 *
 */
public class ClasspathPackage extends FileBasedPackage {

  private String groupId;
  private String artifactId;

  public ClasspathPackage(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
  }
  
  public boolean exists() {
    return resourceExists("environment");
  }

  @Override
  public InputSupplier<InputStream> getResource(String name) throws IOException {
    String url = resourceUrl(name);
    return Resources.newInputStreamSupplier(
        Resources.getResource(url));
  }

  private String resourceUrl(String name) {
    return 
        groupId.replace('.', '/') +
        "/" +
        artifactId.replace('.',  '/') + 
        "/" +
        name;
  }

  @Override
  public boolean resourceExists(String name) {
    InputStream in = getClass().getResourceAsStream("/" + resourceUrl(name));
    if(in == null) {
      return false;
    } else {
      Closeables.closeQuietly(in);
      return true;
    }
  }
}
