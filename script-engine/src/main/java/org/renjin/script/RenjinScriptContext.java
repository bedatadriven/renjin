package org.renjin.script;

import org.renjin.eval.Context;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class RenjinScriptContext implements ScriptContext {

  private final Context context;
  private final Map<String,Object> attributes = new TreeMap<>();
  private final RenjinBindings engineBindings;
  
  RenjinScriptContext(Context context) {
    this.context = context;
    this.engineBindings = new RenjinBindings(context.getEnvironment().getFrame());
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
  public Bindings getBindings(int scope) {
    switch(scope) {
      case ScriptContext.ENGINE_SCOPE:
        return engineBindings;

      default:
      case ScriptContext.GLOBAL_SCOPE:
        throw new UnsupportedOperationException();

    }
  }

  @Override
  public Writer getErrorWriter() {
    return context.getSession().getConnectionTable().getStderr().getPrintWriter();
  }

  @Override
  public Reader getReader() {
    return context.getSession().getStdIn();
  }

  @Override
  public List<Integer> getScopes() {
    return Collections.singletonList(ScriptContext.ENGINE_SCOPE);
  }

  @Override
  public Writer getWriter() {
    return context.getSession().getStdOut();
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
    context.getSession().setStdErr(new PrintWriter(errorWriter));
  }

  @Override
  public void setReader(Reader reader) {
    context.getSession().setStdIn(reader);
  }

  @Override
  public void setWriter(Writer writer) {
    context.getSession().setStdOut(new PrintWriter(writer));
  }

}
