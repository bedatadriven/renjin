/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */
package org.renjin.packaging.test;

import java.io.IOException;
import java.io.OutputStream;


/**
 * An OutputStream which limits total output to 50kb. We use
 * this for write test output files so that infinite loops in
 * tests don't fill up the entire disk and bring the build
 * process to a halt.
 */
public class CappedOutputStream extends OutputStream {
  private final OutputStream out;
  private final int maxBytes;
  private int bytesWritten = 0;
  private boolean capped = false;

  public CappedOutputStream(int maxBytes, OutputStream out) {
    this.out = out;
    this.maxBytes = maxBytes;
  }

  @Override
  public void write(int i) throws IOException {
    if(!capped) {
      out.write(i);
      bytesWritten += i;
      if(bytesWritten > maxBytes) {
        cap();
      }
    }
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void close() throws IOException {
    out.close();
  }

  @Override
  public void write(byte[] bytes, int offset, int len) throws IOException {
    if(!capped) {
      if(bytesWritten + len > maxBytes) {
        out.write(bytes, offset, maxBytes - bytesWritten);
        cap();
      } else {
        out.write(bytes, offset, len);
        bytesWritten += len;
      }
    }
  }

  private void cap() throws IOException {
    out.write("\n----MAX OUTPUT REACHED----\n".getBytes());
    out.flush();
    capped = true;
  }
}
