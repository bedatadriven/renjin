/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A (more) abstract representation of file and directory pathnames.
 *
 *
 */
public abstract class FileInfo {

  /**
   *
   * @return  true if the file exists.
   */
  public abstract boolean exists();

   /**
     * Tests whether the file denoted by this abstract pathname is a normal
     * file.  A file is <em>normal</em> if it is not a directory and, in
     * addition, satisfies other system-dependent criteria.  Any non-directory
     * file created by a Java application is guaranteed to be a normal file.
     *
     * @return  <code>true</code> if and only if the file denoted by this
     *          abstract pathname exists <em>and</em> is a normal file;
     *          <code>false</code> otherwise
     *
     * @throws  SecurityException
     *          If a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          method denies read access to the file
     */
  public abstract boolean isFile();

  public abstract boolean isDirectory() ;

  /**
   *
   * @return  unix-style file mode integer
   */
  public int getMode() {
    int access = 0;
    if(canRead()) {
      access += 4;
    }
    if(canWrite()) {
      access += 2;
    }
    if(isDirectory()) {
      access += 1;
    }
    // i know this is braindead but i can't be bothered
    // to do octal math at the moment
    String digit = Integer.toString(access);
    String octalString = digit + digit + digit;

    return Integer.parseInt(octalString, 8);
  }

  protected abstract boolean canWrite();

  protected abstract boolean canRead();

  /**
   *
   * @return file length, in bytes
   */
  public abstract long length();

  /**
   *
   * @return the time at which the file was last modified, as unix-style date
   */
  public abstract long lastModified();

  public abstract String getName();

  public abstract List<FileInfo> listFiles();

  public abstract String getPath();

  public abstract boolean isHidden();

  public abstract FileInfo getChild(String name);

  public abstract InputStream openInputStream() throws IOException;
}
