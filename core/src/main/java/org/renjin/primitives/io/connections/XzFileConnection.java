package org.renjin.primitives.io.connections;

import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.io.OutputStream;

public class XzFileConnection extends FileConnection {
  public static final int[] XZ_MAGIC_BYTES = { 0xFD, '7', 'z', 'X', 'Z', 0x00 };

  public XzFileConnection(FileObject file) throws IOException {
    super(file);
  }


  @Override
  protected OutputStream doOpenForOutput() throws IOException {
    return new XZCompressorOutputStream(super.doOpenForOutput());
  }
}
