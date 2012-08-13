package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;
import java.util.jar.JarEntry;


public class FastJarFileObject    
extends AbstractFileObject
implements FileObject {   

  private final FastJarFileSystem fs;
  protected JarEntry entry;
  private FileType type;

  protected FastJarFileObject(AbstractFileName name,
      JarEntry entry,
      FastJarFileSystem fs) throws FileSystemException {
    super(name, fs);
    this.fs = fs;
    setZipEntry(entry);
  }

  /**
   * Sets the details for this file object.
   */
  protected void setZipEntry(final JarEntry entry)
  {
    if (this.entry != null)
    {
      return;
    }

    if ((entry == null) || (entry.isDirectory()))
    {
      type = FileType.FOLDER;
    }
    else
    {
      type = FileType.FILE;
    }

    this.entry = entry;
  }


  /**
   * Determines if this file can be written to.
   *
   * @return <code>true</code> if this file is writeable, <code>false</code> if not.
   */
  public boolean isWriteable() throws FileSystemException
  {
    return false;
  }

  /**
   * Returns the file's type.
   */
  protected FileType doGetType() {
    return type;
  }

  /**
   * Lists the children of the file.
   */
  protected String[] doListChildren() {
    return fs.listChildren(entry.getName());
  }

  /**
   * Returns the size of the file content (in bytes).  Is only called if
   * {@link #doGetType} returns {@link FileType#FILE}.
   */
  protected long doGetContentSize() {
    return entry.getSize();
  }

  /**
   * Returns the last modified time of this file.
   */
  protected long doGetLastModifiedTime() throws Exception {
    return entry.getTime();
  }

  /**
   * Creates an input stream to read the file content from.  Is only called
   * if  {@link #doGetType} returns {@link FileType#FILE}.  The input stream
   * returned by this method is guaranteed to be closed before this
   * method is called again.
   */
  protected InputStream doGetInputStream() throws Exception {
    return fs.getInputStream(entry);
  }
}
