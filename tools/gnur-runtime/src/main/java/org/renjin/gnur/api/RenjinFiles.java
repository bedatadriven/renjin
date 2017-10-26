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
package org.renjin.gnur.api;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.FileHandle;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.RecordUnitPtr;
import org.renjin.primitives.Native;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.renjin.gcc.runtime.Stdlib.nullTerminatedString;

/**
 * Provides Renjin-specific hooks into the Session's FileSystemManager
 */
public class RenjinFiles {

  public static Ptr fopen(Ptr filename, Ptr mode) {
    String filenameString = nullTerminatedString(filename);
    String modeString = nullTerminatedString(mode);

    FileObject fileObject;
    try {
      fileObject = Native.currentContext().resolveFile(filenameString);
    } catch (FileSystemException e) {
      return BytePtr.NULL;
    }

    try {
      return new RecordUnitPtr<>(fopen(fileObject, modeString));

    } catch (IOException e) {
      return BytePtr.NULL;
    }
  }

  private static FileHandle fopen(FileObject fileObject, String mode) throws FileSystemException {
    switch (mode) {
      case "rb":
        FileContent content = fileObject.getContent();
        return new InputStreamHandle(content.getInputStream());

      default:
        throw new UnsupportedOperationException("mode: " + mode);
    }
  }

  private static class RandomAccessHandle implements FileHandle {

    private RandomAccessContent content;

    public RandomAccessHandle(RandomAccessContent content) {
      this.content = content;
    }

    @Override
    public int read() throws IOException {
      try {
        return content.readUnsignedByte();
      } catch (EOFException e) {
        return -1;
      }
    }

    @Override
    public void seekSet(long offset) throws IOException {
      content.seek(offset);
    }

    @Override
    public void seekCurrent(long offset) throws IOException {
      throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void seekEnd(long offset) {
      throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void close() throws IOException {
      content.close();
    }
  }

  private static class InputStreamHandle implements FileHandle {

    private InputStream inputStream;
    private long position = 0;

    public InputStreamHandle(InputStream inputStream) {
      this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
      int b = inputStream.read();
      if(b != -1) {
        position ++;
      }
      return b;
    }

    @Override
    public void close() throws IOException {
      inputStream.close();
    }

    @Override
    public void seekSet(long offset) throws IOException {
      if(offset < position) {
        throw new IOException("Cannot rewind the stream");
      }
      long toSkip = offset - position;
      long skipped = inputStream.skip(toSkip);
      if(skipped < toSkip) {
        throw new EOFException();
      }
    }

    @Override
    public void seekCurrent(long offset) throws IOException {
      long skipped = inputStream.skip(offset);
      if(skipped < offset) {
        throw new EOFException();
      }
    }

    @Override
    public void seekEnd(long offset) {
      throw new UnsupportedOperationException("TODO");
    }
  }

}
