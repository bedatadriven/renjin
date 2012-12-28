package org.renjin.script;

import org.renjin.eval.Context;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.util.List;


public class RenjinScriptContext implements ScriptContext{

  private Context context;
  private Reader reader;
  private Writer writer;
  private Writer errorWriter;
  
  RenjinScriptContext(Context context) {
    this.context = context;
  }
  
  public Context getContext() {
    return context;
  }
  
  @Override
  public Object getAttribute(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getAttribute(String arg0, int arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getAttributesScope(String arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Bindings getBindings(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Writer getErrorWriter() {
    return context.getSession().getConnectionTable().getStderr().getPrintWriter();
  }

  @Override
  public Reader getReader() {
    return reader;
  }

  @Override
  public List<Integer> getScopes() {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public Writer getWriter() {
    return writer;
  }

  @Override
  public Object removeAttribute(String arg0, int arg1) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public void setAttribute(String arg0, Object arg1, int arg2) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public void setBindings(Bindings arg0, int arg1) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public void setErrorWriter(Writer errorWriter) {
    this.errorWriter = errorWriter;
  }

  @Override
  public void setReader(Reader reader) {
    this.reader = reader;
  }

  @Override
  public void setWriter(Writer writer) {
    this.writer = writer;
  }

}
