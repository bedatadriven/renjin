package org.renjin.primitives.io.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

public abstract class AbstractConnection implements Connection {

  private BufferedReader reader;
  private PrintWriter writer;
  
  @Override
  public final BufferedReader getReader() throws IOException {
    if(this.reader == null) {
      this.reader =
          new BufferedReader(
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
  
  protected abstract void closeInputIfOpen() throws IOException;
  protected abstract void closeOutputIfOpen() throws IOException;
}
