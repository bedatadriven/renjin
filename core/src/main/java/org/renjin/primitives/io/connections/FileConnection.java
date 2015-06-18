
package org.renjin.primitives.io.connections;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.EvalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;


public class FileConnection extends AbstractConnection {

  private InputStream in;  
  private OutputStream out;
  
  private FileObject file;
  private OpenSpec openSpec = null;
  
  public FileConnection(FileObject file) throws IOException {
    this.file = file;
  }
  
  @Override
  public void open(OpenSpec spec) throws IOException {
    this.openSpec = spec;
    if(spec.forReading() && spec.forWriting()) {
      throw new EvalException("Read/write file connections not yet implemented");
    }
    if(spec.forReading()) {
      if(spec.isBinary()) {
        assureOpenForInput();
      } else {
        getReader();
      }
    } else if(spec.forWriting()) {
      if(spec.isBinary()) {
        assureOpenForOutput();
      } else {
        getPrintWriter();
      }
    }
  }

  private final InputStream assureOpenForInput() throws IOException {
    if(out != null) {
      throw new EvalException("connection is already opened for output, cannot open for input");
    }
    if(in == null) {
      this.in = doOpenForInput();
    }
    return this.in;
  }

  protected InputStream doOpenForInput() throws IOException {
    // We want to automatically decompress if the underlying file is gzipped
    int pushBackBufferSize = 2;
    PushbackInputStream in = new PushbackInputStream(file.getContent().getInputStream(),
        pushBackBufferSize);
    int b1 = in.read();
    int b2 = in.read();
    in.unread(b2);
    in.unread(b1);
    if(b1 == GzFileConnection.GZIP_MAGIC_BYTE1 && b2 == GzFileConnection.GZIP_MAGIC_BYTE2) {
      return new GZIPInputStream(in);
    } else {
      return in;
    }
  }
  
  private OutputStream assureOpenForOutput() throws IOException {
    if(in != null) {
      throw new EvalException("connection is already opened for input, cannot open for output");
    }
    if(out == null) {
      this.out = doOpenForOutput();
    }
    return this.out;
  }

  protected OutputStream doOpenForOutput() throws FileSystemException, IOException {
    return file.getContent().getOutputStream(openSpec.isAppend());
  }
  
  @Override
  public final InputStream getInputStream() throws IOException {
    return assureOpenForInput();
  }

  @Override
  public final OutputStream getOutputStream() throws IOException {
    return assureOpenForOutput();
  }

  @Override
  protected void closeInputIfOpen() throws IOException {
    if(in != null) {
      in.close();
    }
  }

  @Override
  protected void closeOutputIfOpen() throws IOException {
    if(out != null) {
      out.close();
    }
  }

  @Override
  public boolean isOpen() {
    return in!=null || out!=null;
  }

  @Override
  public String getClassName() {
    return "file";
  }

  @Override
  public String getDescription() {
    return file.getName().getPath();
  }

  @Override
  public Type getType() {
    if(openSpec == null) {
      return Type.TEXT;
    } else {
      return openSpec.getType();
    }
  }

  @Override
  public String getMode() {
    if(openSpec == null) {
      return "r";
    } else {
      return openSpec.toString();
    }
  }

  @Override
  public boolean canRead() {
    return !isOpen() || openSpec == null || openSpec.forReading();
  }

  @Override
  public boolean canWrite() {
    return !isOpen() || openSpec == null || openSpec.forWriting();
  }
  
}
