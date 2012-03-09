package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnection implements Connection {

  private final Socket socket;
  private Reader reader;
  private PrintWriter writer;
  
  public SocketConnection(String host, int port) throws UnknownHostException, IOException {
    this.socket = new Socket(host, port);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return socket.getOutputStream();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return socket.getInputStream();
  }

  @Override
  public Reader getReader() throws IOException {
    if(this.reader == null) {
      this.reader = new InputStreamReader(getInputStream());
    }
    return this.reader;
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    if(this.writer == null) {
      this.writer = new PrintWriter(getOutputStream());
    }
    return this.writer;
  }

  @Override
  public void close() throws IOException {
    if(this.writer != null) {
      this.writer.flush();
    }
    socket.close();
  }

}
