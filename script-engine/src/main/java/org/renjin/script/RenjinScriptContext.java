/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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

  private Context context;
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
