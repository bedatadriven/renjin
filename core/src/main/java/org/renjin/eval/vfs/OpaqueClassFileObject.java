package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

class OpaqueClassFileObject extends AbstractFileObject {

  private final URL resource;

  public OpaqueClassFileObject(AbstractFileName name, AbstractFileSystem fs, URL resource) {
    super(name, fs);
    this.resource = resource;
  }

  @Override
  protected FileType doGetType() throws Exception {
    URLConnection conn = resource.openConnection();
    if(conn instanceof JarURLConnection) {
      JarURLConnection jarConnection = (JarURLConnection) conn;
      if (jarConnection.getJarEntry().isDirectory()) {
        return FileType.FOLDER;
      } else {
        return FileType.FILE;
      }
    }
    return FileType.FILE_OR_FOLDER;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    return new String[0];
  }

  @Override
  protected long doGetContentSize() throws Exception {
    URLConnection conn = resource.openConnection();
    long length = conn.getContentLengthLong();
    if(length < 0) {
      throw new UnsupportedOperationException("Could not get content size from " + resource);
    }
    return length;
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    return resource.openStream();
  }
}
