package org.renjin.primitives.packaging;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.renjin.eval.EvalException;
import org.renjin.util.NamedByteSource;

import java.io.IOException;
import java.net.URL;

/**
 * Provides access to a Renjin package that is on the application's classpath.
 */
public class ClasspathPackage extends FileBasedPackage {

  private ClassLoader classLoader;

  public ClasspathPackage(ClassLoader classLoader, FqPackageName name) {
    super(name);
    this.classLoader = classLoader;
  }
  
  public ClasspathPackage(FqPackageName name) {
    super(name);
  }
  
  public boolean exists() {
    return resourceExists("environment");
  }

  @Override
  public ByteSource getResource(String name) throws IOException {
    String qualifiedName = qualifyResourceName(name);
    URL url = classLoader.getResource(qualifiedName);
    if(url == null) {
      throw new IOException(String.format("Could not find %s (%s)", name, qualifiedName));
    }
  public NamedByteSource getResource(String name) throws IOException {
    String url = resourceUrl(name);
    try {
      return Resources.asByteSource(url);
      return new NamedByteSource(url,Resources.asByteSource(Resources.getResource(url)));
    } catch(Exception e) {
      throw new IOException(String.format("Could not load %s (%s)", name, url.toString()), e);
    }
  }

  @Override
  public Class loadClass(String name) {
    try {
      return classLoader.loadClass(name);
    } catch (ClassNotFoundException e) {
      throw new EvalException(e.getMessage(), e);
    }
  }

  private String qualifyResourceName(String name) {
    return
        getName().getGroupId().replace('.', '/') +
        "/" +
        getName().getPackageName() +
        "/" +
        name;
  }

  @Override
  public boolean resourceExists(String name) {
      URL url = classLoader.getResource(qualifyResourceName(name));
      return url != null;
  }
}
