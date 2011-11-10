package r.scripting;

import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import r.jvmi.r2j.converters.Converters;
import r.lang.Context;
import r.lang.EvalResult;
import r.lang.HashFrame;
import r.lang.SEXP;
import r.lang.Symbol;
import r.parser.RParser;

public class RenjinScriptEngine implements ScriptEngine {

  private final RenjinScriptEngineFactory factory;
  private final Context topLevelContext;
  
  RenjinScriptEngine(RenjinScriptEngineFactory factory) {
    super();
    this.factory = factory;
    this.topLevelContext = Context.newTopLevelContext();
    try {
      topLevelContext.init();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Override
  public Bindings createBindings() {
    return new RenjinBindings(new HashFrame());
  }
  
  @Override
  public Object eval(Reader reader, Bindings n) throws ScriptException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(Reader reader) throws ScriptException {
    try {
      return eval(topLevelContext, RParser.parseSource(reader));
    } catch (IOException e) {
      throw new ScriptException(e);
    }
  }

  @Override
  public Object eval(String script, Bindings n) throws ScriptException {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public Object eval(String script) throws ScriptException {
    return eval(topLevelContext, RParser.parseSource(script + "\n"));
  }

  @Override
  public Object get(String key) {
    return topLevelContext.getEnvironment().getVariable(Symbol.get(key));
  }

  @Override
  public Bindings getBindings(int scope) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScriptContext getContext() {
    return new RenjinScriptContext(topLevelContext);
  }

  @Override
  public void put(String key, Object value) {
    topLevelContext.getEnvironment().setVariable(Symbol.get(key), 
        Converters.get(value.getClass()).convertToR(value));
  }

  @Override
  public void setBindings(Bindings bindings, int scope) {

  }

  @Override
  public void setContext(ScriptContext context) {
    throw new UnsupportedOperationException("Cannot set the context");
  }

  @Override
  public Object eval(String script, ScriptContext scriptContext)
      throws ScriptException {
    SEXP source = RParser.parseSource(script + "\n");
    return eval(unwrapContext(scriptContext), source);
  }

  @Override
  public Object eval(Reader reader, ScriptContext scriptContext)
      throws ScriptException {
    
    SEXP source;
    try {
      source = RParser.parseSource(reader);
    } catch (IOException e) {
      throw new ScriptException(e);
    }
    return eval(unwrapContext(scriptContext), source);
  }
  
  private Object eval(Context context, SEXP source) {
    EvalResult result = source.evaluate(context, context.getEnvironment());
    return result.getExpression();
  }

  private Context unwrapContext(ScriptContext scriptContext) {
    return ((RenjinScriptContext)scriptContext).getContext();
  }

  @Override
  public ScriptEngineFactory getFactory() {
    return factory;
  }
}
