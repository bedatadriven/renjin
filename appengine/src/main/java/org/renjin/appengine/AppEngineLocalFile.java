package org.renjin.appengine;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;

/**
 * Wraps a LocalFileObject to avoid triggering SecurityExceptions
 *
 */
public class AppEngineLocalFile extends LocalFile {

  public AppEngineLocalFile(LocalFileSystem fileSystem, String rootFile,
      AbstractFileName name) throws FileSystemException {
    super(fileSystem, rootFile, name);
  }

  @Override
  protected boolean doIsWriteable() throws FileSystemException {
    return false;
  }
    
}
