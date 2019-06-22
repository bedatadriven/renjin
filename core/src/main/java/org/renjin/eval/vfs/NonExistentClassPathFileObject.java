package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.io.InputStream;

class NonExistentClassPathFileObject extends AbstractFileObject {

  /**
   * @param name the file name - muse be an instance of {@link AbstractFileName}
   * @param fs   the file system
   * @throws ClassCastException if {@code name} is not an instance of {@link AbstractFileName}
   */
  NonExistentClassPathFileObject(AbstractFileName name, AbstractFileSystem fs) {
    super(name, fs);
  }

  @Override
  protected FileType doGetType() throws Exception {
    return FileType.IMAGINARY;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected long doGetContentSize() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    throw new UnsupportedOperationException();
  }
}
