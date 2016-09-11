package org.renjin.primitives.io.connections;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.io.OutputStream;

public class BzipFileConnection extends FileConnection {

  public BzipFileConnection(FileObject file) throws IOException {
    super(file);
  }


  @Override
  protected OutputStream doOpenForOutput() throws IOException {
    return new BZip2CompressorOutputStream(super.doOpenForOutput());
  }
}
