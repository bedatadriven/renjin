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

package org.renjin.primitives.io.connections;

import org.apache.commons.vfs.FileObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
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
  protected InputStream doOpenForInput() throws IOException {
    InputStream in = super.doOpenForInput();
    in.mark(2);
    boolean isCompressed =
        in.read() == GZIP_MAGIC_BYTE1 &&
        in.read() == GZIP_MAGIC_BYTE2;
    in.reset();

    if(isCompressed) {
      return new GZIPInputStream(in);
    } else {
      return in;
    }
  }
  
  @Override
  protected OutputStream doOpenForOutput() throws IOException {
    return new GZIPOutputStream(super.doOpenForOutput());
  }


}
