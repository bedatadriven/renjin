package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import org.renjin.eval.EvalException;

public class TextConnection implements Connection {

  private final String objectName;
  private PushbackBufferedReader reader;
  
  public TextConnection(String objectName, String text) {
    super();
    this.objectName = objectName;
    this.reader = new PushbackBufferedReader(new StringReader(text));
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    if(!spec.forReading()) {
      throw new EvalException("Only read support for text connections is implemented, sorry!");
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    throw new EvalException("reading bytes from TextConnections is not supported");
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new EvalException("Writing to textConnections is not currently implemented");
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public String getClassName() {
    return "textConnection";
  }

  @Override
  public String getDescription() {
    return objectName;
  }

  @Override
  public Type getType() {
    return Type.TEXT;
  }

  @Override
  public PushbackBufferedReader getReader() throws IOException {
    return reader;
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    throw new EvalException("reading bytes from TextConnections is not supported");
  }

  @Override
  public void close() throws IOException {

  }

  @Override
  public String getMode() {
    return "r";
  }

  @Override
  public boolean canRead() {
    return true;
  }

  @Override
  public boolean canWrite() {
    return false;
  }

}
