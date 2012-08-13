package org.renjin.primitives.io.serialization;

import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

/**
 * Provides read access to an .rdb file.
 */
public class RDatabase {

  private byte[] content;

  public RDatabase(FileObject databaseFile) throws IOException {
    long len = databaseFile.getContent().getSize();
    if(len > Integer.MAX_VALUE) {
      throw new IOException("Rdb file too big to load into memory: " + len);
    }
    content = new byte[(int)len];
    InputStream in = databaseFile.getContent().getInputStream();
    int totalBytesRead=0;
    while(totalBytesRead < len) {
      totalBytesRead += in.read(content, totalBytesRead, ((int)len)-totalBytesRead);
    }
  }

  public byte[] getBytes(int offset, int length) throws IOException,
      DataFormatException {
    byte bytes[] = new byte[length];
    java.lang.System.arraycopy(content, offset, bytes, 0, length);
    return bytes;
  }

}
