package org.renjin.maven.test;

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
