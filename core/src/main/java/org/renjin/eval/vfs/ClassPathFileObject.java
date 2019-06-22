package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classpath resource that resolves to a simple File
 */
class ClassPathFileObject extends AbstractFileObject  {

  private final File file;

  public ClassPathFileObject(ClassPathFileSystem fs, AbstractFileName name, File file) {
    super(name, fs);
    this.file = file;
  }

  @Override
  protected FileType doGetType() {
    if(file.exists()) {
      if(file.isDirectory()) {
        return FileType.FOLDER;
      } else {
        return FileType.FILE;
      }
    } else {
      return FileType.IMAGINARY;
    }
  }

  @Override
  protected boolean doIsWriteable() {
    return false;
  }

  @Override
  protected boolean doIsReadable() {
    return true;
  }

  @Override
  protected String[] doListChildren() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected long doGetContentSize() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected InputStream doGetInputStream() throws IOException {
    return new FileInputStream(file);
  }

  public File getFile() {
    return file;
  }
}
