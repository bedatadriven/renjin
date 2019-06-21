/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.runtime;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * C standard library file handle
 */
public class FileHandleImpl extends AbstractFileHandle {

  private RandomAccessFile file;

  public FileHandleImpl(RandomAccessFile file) {
    this.file = file;
  }

  @Override
  public int read() throws IOException {
    return file.read();
  }

  @Override
  public void write(int b) throws IOException {
    file.write(b);
  }

  @Override
  public void rewind() throws IOException {
    file.seek(0);
  }

  @Override
  public void flush() throws IOException {
    file.getFD().sync();
  }

  @Override
  public void close() throws IOException {
    file.close();
  }

  @Override
  public void seekSet(long offset) throws IOException {
    file.seek(offset);
  }

  @Override
  public void seekCurrent(long relativeOffset) throws IOException {
    file.seek(file.getFilePointer() + relativeOffset);
  }

  @Override
  public void seekEnd(long relativeOffset) throws IOException {
    long offset = file.length() - relativeOffset - 1;
    file.seek(offset);
  }

  @Override
  public boolean isEof() {
    try {
      return file.getFilePointer() >= file.length();
    } catch (IOException e) {
      return true;
    }
  }

  @Override
  public long position() throws IOException {
    return file.getFilePointer();
  }
}
