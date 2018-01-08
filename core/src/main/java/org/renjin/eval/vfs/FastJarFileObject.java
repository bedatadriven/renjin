/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;
import java.util.jar.JarEntry;


public class FastJarFileObject extends AbstractFileObject implements FileObject {

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
