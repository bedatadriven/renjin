package org.renjin.eval.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;

class ClassPathFileSystem extends AbstractFileSystem {

  private ClassLoader classLoader;

  protected ClassPathFileSystem(ClassLoader classLoader, FileName rootName, FileSystemOptions fileSystemOptions) {
    super(rootName, null, fileSystemOptions);
    this.classLoader = classLoader;
  }


  @Override
  protected FileObject createFile(AbstractFileName name) throws Exception {

    // Remove initial '/'
    String resourcePath = name.getPath().substring(1);

    // Search the classpath for the resource
    Enumeration<URL> resource = classLoader.getResources(resourcePath);

    if(!resource.hasMoreElements()) {
      return new NonExistentClassPathFileObject(name, this);
    }

    // Find first non-directory file object
    URL first = resource.nextElement();
    return createFile(name, first);
  }

  private FileObject createFile(AbstractFileName name, URL resource) {
    if(resource.getProtocol().equals("file")) {
      return new ClassPathFileObject(this, name, new File(fileFromUrl(resource)));
    } else {
      return new OpaqueClassFileObject(name, this, resource);
    }
  }

  private String fileFromUrl(URL resource) {
    try {
      return URLDecoder.decode(resource.getFile(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("JVM does not support UTF-8 Encoding");
    }
  }

  @Override
  protected void addCapabilities(Collection<Capability> caps) {
    caps.addAll(ClassPathFileProvider2.CAPABILITIES);
  }

  @Override
  protected File doReplicateFile(FileObject file, FileSelector selector) throws Exception {
    if(file instanceof ClassPathFileObject) {
      return ((ClassPathFileObject) file).getFile();
    }
    return super.doReplicateFile(file, selector);
  }
}
