/**
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

import org.renjin.RVersion;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.repackaged.guava.collect.Lists;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;

public class RenjinScriptEngineFactory implements ScriptEngineFactory {

  public RenjinScriptEngineFactory() {
  }
  
  @Override
  public String getEngineName() {
    return "Renjin";
  }

  @Override
  public String getEngineVersion() {
    return "017";
  }

  @Override
  public List<String> getExtensions() {
    return Lists.newArrayList("R", "r", "S", "s");
  }

  @Override
  public String getLanguageName() {
    return "R";
  }

  @Override
  public String getLanguageVersion() {
    return RVersion.STRING;
  }

  @Override
  public String getMethodCallSyntax(String arg0, String arg1, String... arg2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getMimeTypes() {
    return Lists.newArrayList("text/x-R");
  }

  @Override
  public List<String> getNames() {
    return Lists.newArrayList("Renjin");
  }

  @Override
  public String getOutputStatement(String arg0) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public Object getParameter(String key) {
    // I’m not sure what to do with ScriptEngine.ARGV and
    // ScriptEngine.FILENAME -- not even Rhino JavaScript recognizes these
    // keys.

    if (key.equals (ScriptEngine.ENGINE)) {
      return getEngineName ();
    } else if (key.equals (ScriptEngine.ENGINE_VERSION)) {
      return getEngineVersion ();
    } else if(key.equals (ScriptEngine.NAME)) {
      return getNames ().get (0);
    }  else if (key.equals (ScriptEngine.LANGUAGE)) {
      return getLanguageName ();
    } else if (key.equals (ScriptEngine.LANGUAGE_VERSION)) {
      return getLanguageVersion ();
    } else if (key.equals ("THREADING")) {
      return null; // Until thoroughly tested.
    } else {
      return null;  
    }
  }

  @Override
  public String getProgram(String... arg0) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public RenjinScriptEngine getScriptEngine() {
    return new RenjinScriptEngine(this, new SessionBuilder().withDefaultPackages().build());
  }
  
  public RenjinScriptEngine getScriptEngine(Session session) {
    return new RenjinScriptEngine(this, session);
  }
}
