package org.renjin.appengine;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;

public class AppEngineLocalFileSystem extends LocalFileSystem {

  private final String rootFile;
  
  public AppEngineLocalFileSystem(FileName rootName, String rootFile,
      FileSystemOptions opts) {
    super(rootName, rootFile, opts);
    this.rootFile = rootFile;
  }

  @Override
  protected FileObject createFile(AbstractFileName name)
      throws FileSystemException {
    return new AppEngineLocalFile(this, rootFile, name);
  }

}
