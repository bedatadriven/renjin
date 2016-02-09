package org.renjin.primitives.packaging;

import com.google.common.io.Resources;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
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
    this(ClasspathPackage.class.getClassLoader(), name);
  }
  
  public boolean exists() {
    return resourceExists("environment");
  }

  @Override
  public NamedByteSource getResource(String name) throws IOException {
    String qualifiedName = qualifyResourceName(name);
    URL url = classLoader.getResource(qualifiedName);
    if (url == null) {
      throw new IOException(String.format("Could not find %s (%s)", name, qualifiedName));
    }
    try {
      return new NamedByteSource(name, Resources.asByteSource(url));
    } catch(Exception e) {
      throw new IOException(String.format("Could not load %s (%s)", name, url.toString()), e);
    }
  }

  @Override
  public Class loadClass(String name) throws ClassNotFoundException {
    return classLoader.loadClass(name);
  }

  @Override
  public FileObject resolvePackageRoot(FileSystemManager fileSystemManager) throws FileSystemException {
    // Find the URL where the package is located
    String qualifiedName = qualifyResourceName("environment");
    URL url = classLoader.getResource(qualifiedName);
    
    return fileSystemManager.resolveFile(url.toString()).getParent();
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
