package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public abstract class AbstractConnection implements Connection {

  private PushbackBufferedReader reader;
  private PrintWriter writer;
  
  @Override
  public final PushbackBufferedReader getReader() throws IOException {
    if(this.reader == null) {
      this.reader =
          new PushbackBufferedReader(
          new InputStreamReader(getInputStream()));
    }
    return this.reader;
  }

  @Override
  public final PrintWriter getPrintWriter() throws IOException {
    if(writer == null) {
      this.writer = new PrintWriter(getOutputStream());
    }
    return this.writer;
  }

  @Override
  public void close() throws IOException {
    if(reader != null) {
      reader.close();
    } else {
      closeInputIfOpen();
    }
    if(writer != null) {
      writer.close();
    } else {
      closeOutputIfOpen();
    }
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
    return true;
  }

  protected abstract void closeInputIfOpen() throws IOException;
  protected abstract void closeOutputIfOpen() throws IOException;
}
