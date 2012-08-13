package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;


public class FastJarRootFileObject extends AbstractFileObject {

  private FastJarFileSystem fs;

  protected FastJarRootFileObject(AbstractFileName name, FastJarFileSystem fs) {
    super(name, fs);
    this.fs = fs;
  }

  @Override
  protected FileType doGetType() throws Exception {
    return FileType.FOLDER;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    return fs.getRootItems();
  }

  @Override
  protected long doGetContentSize() throws Exception {
    return 0;
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    throw new FileSystemException("can't open directory!");
  }

}
