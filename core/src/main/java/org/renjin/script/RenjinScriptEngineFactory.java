package org.renjin.script;

import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.renjin.RVersion;

import r.lang.Context;

import com.google.common.collect.Lists;

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
    return Lists.newArrayList(".R");
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
    // i don't know if this is correct...
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
    return new RenjinScriptEngine(this);
  }
  
  public RenjinScriptEngine getScriptEngine(Context context) {
    return new RenjinScriptEngine(this, context);
  }
  
  
  public Builder withOptions() {
    return new Builder();
  }
  
  
  
  public class Builder {
    
    private final Context context;
    
    private Builder() {
      context = Context.newTopLevelContext();
    }
    
    public Builder withNoDefaultPackages() {
      context.getGlobals().systemEnvironment.put("R_DEFAULT_PACKAGES", "NULL");
      return this;
    }
    
    public RenjinScriptEngine get() throws IOException {
      context.init();
      return getScriptEngine(context);
    }
    
  }
}
