package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import r.lang.exception.EvalException;

public class FileConnection extends AbstractConnection {

  private InputStream in;  
  private OutputStream out;
  
  private FileObject file;
  
  public FileConnection(FileObject file, String openMode) throws IOException {
    this.file = file;
    if(openMode.contains("w")) {
      assureOpenForOutput();
    } 
    if(openMode.contains("r")) {
      assureOpenForInput();
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

  protected InputStream doOpenForInput() throws FileSystemException, IOException {
    return file.getContent().getInputStream();
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
    return file.getContent().getOutputStream();
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
  
}
