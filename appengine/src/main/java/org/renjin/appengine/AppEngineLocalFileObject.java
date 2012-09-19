package org.renjin.appengine;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DecoratedFileObject;

/**
 * Wraps a LocalFileObject to avoid triggering SecurityExceptions
 *
 */
public class AppEngineLocalFileObject extends DecoratedFileObject {

  public AppEngineLocalFileObject(FileObject decoratedFileObject) {
    super(decoratedFileObject);
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    return false;
  }
}
