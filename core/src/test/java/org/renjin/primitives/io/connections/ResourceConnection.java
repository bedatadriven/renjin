package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceConnection extends AbstractConnection {
  private Class clazz;
  private String name;
  
  public ResourceConnection(Class clazz, String name) {
    super();
    this.clazz = clazz;
    this.name = name;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    InputStream in = clazz.getResourceAsStream(name);
    if(in == null) {
      throw new IOException("Cannot open resource '" + name + "'");
    }
    return in;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void closeInputIfOpen() throws IOException {
    
  }

  @Override
  protected void closeOutputIfOpen() throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isOpen() {
    return true;
  }
  
  
}
