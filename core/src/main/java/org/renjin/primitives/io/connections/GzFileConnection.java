/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.io.connections;

import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A connection to a gzipped file.
 * The connection can be written to or read from, but once read to
 * it can be written to, and vice-versa
 *
 * If the file is to be read and is not actually compressed, then it will be opened
 * as a regular file.
 *
 */
public class GzFileConnection extends FileConnection {

  public static final int GZIP_MAGIC_BYTE1 = 31;
  public static final int GZIP_MAGIC_BYTE2 = 139;

  public GzFileConnection(FileObject file) throws IOException {
    super(file);
  }

  @Override
  protected OutputStream doOpenForOutput() throws IOException {
    return new GZIPOutputStream(super.doOpenForOutput());
  }
}
