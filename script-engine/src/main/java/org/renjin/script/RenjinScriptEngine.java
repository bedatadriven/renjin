/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.invoke.reflection.converters.RuntimeConverter;
import org.renjin.parser.RParser;
import org.renjin.primitives.Warning;
import org.renjin.primitives.special.BreakException;
import org.renjin.primitives.special.NextException;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.sexp.*;

import javax.script.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RenjinScriptEngine implements ScriptEngine, Invocable {

  private final RenjinScriptEngineFactory factory;

  // context from renjin core:
  private final Context topLevelContext;

  // jsr context, which wrap renjincore context.
  private final ScriptContext scriptContext;
  
  RenjinScriptEngine(RenjinScriptEngineFactory factory, Session session) {
    super();
    this.factory = factory;
    this.topLevelContext = session.getTopLevelContext();
    this.scriptContext = new RenjinScriptContext(topLevelContext);
  }

  public Session getSession() {
    return topLevelContext.getSession();
  }
  
  public Context getTopLevelContext() {
    return topLevelContext;
  }
  
  @Override
  public Bindings createBindings() {
    return new RenjinBindings(new HashFrame());
  }
  
  @Override
  public Object get(String key) {
    return topLevelContext.getEnvironment().getVariable(topLevelContext, Symbol.get(key));
  }

  @Override
  public Bindings getBindings(int scope) {
    switch(scope) {
      case ScriptContext.ENGINE_SCOPE:
        return new RenjinBindings(topLevelContext.getEnvironment().getFrame());

      default:
      case ScriptContext.GLOBAL_SCOPE:
        throw new UnsupportedOperationException();

    }
  }

  @Override
  public ScriptContext getContext() {
    return scriptContext;
  }

  @Override
  public void put(String key, Object value) {
    SEXP convertedValue;
    if(value == null) {
      convertedValue = Null.INSTANCE;
    } else {
      convertedValue = Converters.get(value.getClass()).convertToR(value);
    }
    topLevelContext.getEnvironment().setVariable(topLevelContext, Symbol.get(key), convertedValue);
  }

  @Override
  public void setBindings(Bindings bindings, int scope) {

  }

  @Override
  public void setContext(ScriptContext context) {
    throw new UnsupportedOperationException("Cannot set the context");
  }


  @Override
  public Object eval(Reader reader, Bindings n) throws ScriptException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(String script, Bindings n) throws ScriptException {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public Object eval(String script) throws ScriptException {
    String filename = getFilenameFromContext(scriptContext,INLINE_STRING);
    return eval(topLevelContext, RParser.parseSource(script + "\n", filename));
  }
  
  @Override
  public Object eval(String script, ScriptContext scriptContext)
      throws ScriptException {
    //TODO: agreement to bind name.
    String filename = getFilenameFromContext(scriptContext,INLINE_STRING);
    SEXP source = RParser.parseSource(script + "\n", filename);
    return eval(unwrapContext(scriptContext), source);
  }

  @Override
  public Object eval(Reader reader) throws ScriptException {
    String filename = getFilenameFromContext(scriptContext,INLINE_STRING);
    return eval(reader, topLevelContext, filename);
  }
  
  @Override
  public Object eval(final Reader reader, ScriptContext scriptContext)
      throws ScriptException {
    String filename = getFilenameFromContext(scriptContext,UNKNOWN);
    return eval(reader, unwrapContext(scriptContext), filename);
  }

  private Object eval(Reader reader, Context context, String filename) throws ScriptException {
    SEXP source;
    try {
      // terminate with '\n'
      CharSource terminated = CharSource.concat(
          newReaderSupplier(reader),
          CharSource.wrap("\n"));
      source = RParser.parseSource(terminated, new CHARSEXP(filename) );
    } catch (IOException e) {
      throw new ScriptException(e);
    }
    return eval(context, source);
  }
  
  private Object eval(Context context, SEXP source) {
    try {
      return context.evaluate( source, context.getEnvironment());
    } catch(BreakException e) {
      throw new EvalException("no loop for break");
    } catch(NextException e) {
      throw new EvalException("no loop for next");

    }
  }

  public void eval(File file) throws IOException, ScriptException {
    InputStreamReader reader = new InputStreamReader(
        new FileInputStream(file));
    scriptContext.setAttribute(ScriptEngine.FILENAME,file.getName(),ScriptContext.ENGINE_SCOPE); 
    eval(reader);
    reader.close();
  }
  

  private CharSource newReaderSupplier(final Reader reader) {
    return new CharSource() {
      @Override
      public Reader openStream() throws IOException {
        return reader;
      }      
    };
  }

  private Context unwrapContext(ScriptContext scriptContext) {
    return ((RenjinScriptContext)scriptContext).getContext();
  }

  private String getFilenameFromContext(ScriptContext ctx, String defaultValue) {
    String fileName = defaultValue;
    Object oFileName = scriptContext.getAttribute(ScriptEngine.FILENAME);
    if (oFileName!=null) {
      fileName = oFileName.toString();
    }
    return fileName; 
  }



  private static final String INLINE_STRING="inline-string";
  private static final String UNKNOWN="unknown";

  @Override
  public ScriptEngineFactory getFactory() {
    return factory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getInterface(Class<T> clasz) {
    return (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {clasz}, new InvocationHandler() {
      
      @Override
      public Object invoke(Object instance, Method method, Object[] arguments) throws Exception {
      
        SEXP result = invokeFunction(method.getName(), arguments);
        return Converters.get(method.getReturnType()).convertToJava(result);
      
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getInterface(final Object thiz, Class<T> clasz) {
    return (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {clasz}, new InvocationHandler() {
      
      @Override
      public Object invoke(Object instance, Method method, Object[] arguments) throws Exception {
        SEXP result = invokeMethod(thiz, method.getName(), arguments);
        return Converters.get(method.getReturnType()).convertToJava(result);
      }
    });    
  }

  @Override
  public SEXP invokeFunction(String name, Object... arguments)
      throws ScriptException, NoSuchMethodException {

    if(name == null) {
      throw new NullPointerException("name");
    }
   
    Function function = topLevelContext.getEnvironment().findFunction(topLevelContext, Symbol.get(name));
    if(function == null) {
      throw new NoSuchMethodException(name);
    }

    return invoke(function, arguments);
  }
  
  @Override
  public SEXP invokeMethod(Object thiz, String name, Object... arguments)
      throws ScriptException, NoSuchMethodException {
   
    SEXP element;
    if(thiz instanceof Environment) {
      element = ((Environment)thiz).getVariable(topLevelContext, name);
    } else if(thiz instanceof ListVector) {
      element = ((ListVector)thiz).get(name);
    } else {
      throw new NoSuchMethodException(name);
    }
    if(!(element instanceof Function)) {
      throw new NoSuchMethodException(name);
    }
    Function method = (Function)element;
    
    return invoke(method, arguments);
  }
  
  private SEXP invoke(Function function, Object... arguments) {
    PairList.Builder argList = new PairList.Builder();
    for(Object argument : arguments) {
      argList.add(RuntimeConverter.INSTANCE.convertToR(argument));
    }
   
    FunctionCall call = new FunctionCall(function, argList.build());
  
    return topLevelContext.evaluate(call);
  }
  
  public FunctionCallBuilder invoke(String functionName) {
    return new FunctionCallBuilder(functionName);
  }

  public void printWarnings() {
    SEXP warnings = topLevelContext.getBaseEnvironment().getVariable(topLevelContext, Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
          topLevelContext.getBaseEnvironment());
    }

    topLevelContext.getBaseEnvironment().remove(Warning.LAST_WARNING);
  }
  
  public class FunctionCallBuilder {
    
    private Symbol function;
    private PairList.Builder arguments = new PairList.Builder();
    
    private FunctionCallBuilder(String functionName) {
      this.function = Symbol.get(functionName);
    }
    
    public FunctionCallBuilder withArgument(Object argument) {
      arguments.add(RuntimeConverter.INSTANCE.convertToR(argument));
      return this;
    }
    
    public FunctionCallBuilder withNamedArgument(String name, Object argument) {
      arguments.add(name, RuntimeConverter.INSTANCE.convertToR(argument));
      return this;
    }
    
    public <S extends SEXP> S apply() {
      FunctionCall call = new FunctionCall(function, arguments.build());
      return (S)topLevelContext.evaluate(call);
    }
  }

  public Context getRuntimeContext() {
    return topLevelContext;
  }

}
