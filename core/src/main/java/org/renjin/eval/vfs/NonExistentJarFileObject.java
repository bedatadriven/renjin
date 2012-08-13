package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;


public class NonExistentJarFileObject extends AbstractFileObject {

  protected NonExistentJarFileObject(AbstractFileName name, FastJarFileSystem fs) {
    super(name, fs);
  }

  @Override
  protected FileType doGetType() throws Exception {
    return FileType.IMAGINARY;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    throw new FileSystemException("File doesn't exist");
  }

  @Override
  protected long doGetContentSize() throws Exception {
    throw new FileSystemException("File doesn't exist");
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    throw new FileSystemException("File doesn't exist");
  }
}
