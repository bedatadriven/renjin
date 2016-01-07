package org.renjin.script;

import org.renjin.eval.Context;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class RenjinScriptContext implements ScriptContext{

  private Context context;
  private Reader reader;
  private Writer writer;
  private Writer errorWriter;
  private Map<String,Object> attributes = new TreeMap<>();
  
  RenjinScriptContext(Context context) {
    this.context = context;
  }
  
  public Context getContext() {
    return context;
  }
  
  @Override
  public Object getAttribute(String arg0) {
    return attributes.get(arg0);
  }

  @Override
  public Object getAttribute(String arg0, int arg1) {
    return attributes.get(arg0);
  }

  @Override
  public int getAttributesScope(String arg0) {
    return ScriptContext.ENGINE_SCOPE;
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
    return Arrays.asList(ScriptContext.ENGINE_SCOPE);
  }

  @Override
  public Writer getWriter() {
    return writer;
  }

  @Override
  public Object removeAttribute(String arg0, int arg1) {
      return attributes.remove(arg0);
  }

  @Override
  public void setAttribute(String name, Object value, int scope) {
      if (scope == ScriptContext.ENGINE_SCOPE) {
        attributes.put(name,value);
      } else {
        throw new UnsupportedOperationException(
          String.format("setting attribute in scope (%d) not supported", scope));
      }
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
