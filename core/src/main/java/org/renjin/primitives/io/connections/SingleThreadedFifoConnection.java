package org.renjin.primitives.io.connections;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

/**
 * Creates a circular connection that can be first written to
 * and subsequently read. 
 * 
 * <p>A multithreaded version of this class would use PipedInputStream/
 * PipedOutputStream, but that blocks, and so won't work when the reader and
 * writer are on the same thread. What we'll do here is just assume that 
 * reading and writing is taking place in sequence.
 */
public class SingleThreadedFifoConnection implements Connection {

  // TODO: this is a very crude implementation,
  // it assumes that the byte buffer is read completely each time
  
  private ByteArrayOutputStream out;
  private BufferedReader reader;
  private PrintWriter writer;
  
  public SingleThreadedFifoConnection() {
    out = new ByteArrayOutputStream();
    writer = new PrintWriter(out);
  }
  
  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(getContent());
  }

  protected byte[] getContent() {
    writer.flush();
    byte[] content = out.toByteArray();
    out.reset();
    return content;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return out; 
  }


  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return this.reader = new BufferedReader(
        new InputStreamReader(
            new ByteArrayInputStream(getContent())));
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    return writer;
  }

  @Override
  public void close() throws IOException {
    
  }  
}
