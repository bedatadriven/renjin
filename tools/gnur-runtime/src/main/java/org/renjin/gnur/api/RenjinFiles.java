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
package org.renjin.gnur.api;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;
import org.renjin.gcc.runtime.*;
import org.renjin.primitives.Native;
import org.renjin.primitives.io.connections.GzFileConnection;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import static org.renjin.gcc.runtime.Stdlib.nullTerminatedString;

/**
 * Provides Renjin-specific hooks into the Session's FileSystemManager
 */
public class RenjinFiles {

  public static Ptr R_fopen(Ptr filename, Ptr mode) {
    return fopen(filename, mode);
  }

  public static Ptr R_gzopen (Ptr path, Ptr mode) throws IOException {
    FileObject fileObject = resolveFileObject(path);
    if(fileObject == null) {
      return BytePtr.NULL;
    }

    FileContent content;
    try {
      content = fileObject.getContent();
    } catch (FileSystemException e) {
      return BytePtr.NULL;
    }

    switch (Stdlib.nullTerminatedString(mode)) {
      case "r":
      case "rb":
        return new RecordUnitPtr<>(new InputStreamHandle(() -> openGzInputStream(content)));

      default:
        throw new UnsupportedOperationException("mode: " + mode);
    }
  }

  private static InputStream openGzInputStream(FileContent content) throws IOException {
    InputStream in = content.getInputStream();
    if(in.markSupported()) {
      in.mark(2);
    }
    int b1 = in.read();
    int b2 = in.read();
    boolean gzip = (b1 == GzFileConnection.GZIP_MAGIC_BYTE1 && b2 == GzFileConnection.GZIP_MAGIC_BYTE2);

    if(in.markSupported()) {
      in.reset();
    } else {
      in.close();
      in = content.getInputStream();
    }

    if(gzip) {
      return new GZIPInputStream(in);
    } else {
      return in;
    }
  }

  public static Ptr R_gzgets(Ptr file, Ptr buf, int len) {
    return Stdlib.fgets(buf, len, file);
  }

  public static int R_gzclose(Ptr file) throws IOException {
    FileHandle handle = (FileHandle) file.getArray();
    handle.close();
    return 0;
  }

  public static Ptr R_popen(Ptr filename, Ptr mode) {
    throw new UnsupportedOperationException("R_popen");
  }

  public static int pclose(Ptr stream) {
    throw new UnsupportedOperationException("pclose");
  }

  public static Ptr fopen64(Ptr filename, Ptr mode) {
    return fopen(filename, mode);
  }

  public static Ptr fopen(Ptr filename, Ptr mode) {
    String modeString = nullTerminatedString(mode);

    FileObject fileObject = resolveFileObject(filename);
    if(fileObject == null) {
      return BytePtr.NULL;
    }

    try {
      return new RecordUnitPtr<>(fopen(fileObject, modeString));

    } catch (IOException e) {
      return BytePtr.NULL;
    }
  }

  private static FileObject resolveFileObject(Ptr filename) {
    String filenameString = nullTerminatedString(filename);
    FileObject fileObject;
    try {

      // Intercept requests for R_HOME/library/{packageName}/xx
      Context context = Native.currentContext();
      String homeDirectory = context.getSession().getHomeDirectory();
      String libraryDirectory = homeDirectory + "/library/";

      if(filenameString.startsWith(libraryDirectory)) {
        String relativePath = filenameString.substring(libraryDirectory.length()).replace('\\', '/');
        int packageEnd = relativePath.indexOf('/');
        String packageName = relativePath.substring(0, packageEnd);
        String packageFile = relativePath.substring(packageEnd + 1);

        fileObject = context.getNamespaceRegistry()
            .getNamespace(context, packageName)
            .getPackage()
            .resolvePackageResource(context.getFileSystemManager(), packageFile);

      } else {

        // Otherwise, try a normal file resolution

        fileObject = context.resolveFile(filenameString);

      }
    } catch (FileSystemException e) {
      fileObject = null;
    }
    return fileObject;
  }

  public static void unlink(Ptr fname) throws FileSystemException {
    FileObject fileObject = Native.currentContext().resolveFile(nullTerminatedString(fname));
    fileObject.delete();
  }

  public static Ptr realpath(Ptr path, Ptr resolvedPath) {
    throw new UnimplementedGnuApiMethod("realpath");
  }

  public static int compress(Ptr buf2, int outline, Ptr buf, int len) {
    throw new UnimplementedGnuApiMethod("compress");
  }

  public static int chdir(Ptr path) {
    throw new UnimplementedGnuApiMethod("chdir");
  }

  public static Ptr getcwd(Ptr ptr, int size) {
    throw new UnimplementedGnuApiMethod("getcwd");
  }

  private static FileHandle fopen(FileObject fileObject, String mode) throws IOException {
    switch (mode) {
      case "r":
      case "rb":
        return new InputStreamHandle(() -> fileObject.getContent().getInputStream());

      case "w":
      case "wb":
        return new OutputStreamHandle(fileObject.getContent().getOutputStream());

      case "w+b":
        return new OutputStreamHandle(fileObject.getContent().getOutputStream(true));

      default:
        throw new UnsupportedOperationException("mode: " + mode);
    }
  }

  private interface StreamSupplier<T> {
    T get() throws IOException;
  }

  private static class InputStreamHandle extends AbstractFileHandle {

    private final StreamSupplier<InputStream> supplier;
    private InputStream inputStream;
    private long position = 0;
    private boolean eof;

    public InputStreamHandle(StreamSupplier<InputStream> supplier) throws IOException {
      this.supplier = supplier;
      this.inputStream = supplier.get();
    }

    @Override
    public int read() throws IOException {
      int b = inputStream.read();
      if(b == -1) {
        eof = true;
      } else {
        position ++;
      }
      return b;
    }

    @Override
    public void write(int b) throws IOException {
      throw new UnsupportedOperationException("Cannot write on input stream handle.");
    }

    @Override
    public void rewind() throws IOException {
      inputStream.close();
      inputStream = supplier.get();
      position = 0;
      eof = false;
    }

    @Override
    public void flush() throws IOException {
      throw new UnsupportedOperationException("Cannot flush an input stream handle.");
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

    @Override
    public boolean isEof() {
      return eof;
    }

    @Override
    public long position() throws IOException {
      return position;
    }
  }

  private static class OutputStreamHandle extends AbstractFileHandle {

    private OutputStream outputStream;
    private long position;

    public OutputStreamHandle(OutputStream outputStream) {
      this.outputStream = outputStream;
    }

    @Override
    public int read() throws IOException {
      throw new UnsupportedOperationException("Cannot read from output stream handle.");
    }

    @Override
    public void write(int b) throws IOException {
      outputStream.write(b);
      position++;
    }

    @Override
    public void rewind() throws IOException {
      throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void flush() throws IOException {
      outputStream.flush();
    }

    @Override
    public void close() throws IOException {
      outputStream.close();
    }

    @Override
    public void seekSet(long offset) throws IOException {
      throw new UnsupportedOperationException("TODO");
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
    public boolean isEof() {
      return false;
    }

    @Override
    public long position() throws IOException {
      return position;
    }
  }

}
