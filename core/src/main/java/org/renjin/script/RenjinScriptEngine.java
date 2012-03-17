package org.renjin.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.renjin.primitives.Warning;

import r.jvmi.r2j.converters.Converters;
import r.jvmi.r2j.converters.RuntimeConverter;
import r.lang.Context;
import r.lang.Environment;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.HashFrame;
import r.lang.ListVector;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;
import r.parser.RParser;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

public class RenjinScriptEngine implements ScriptEngine, Invocable {

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

  RenjinScriptEngine(RenjinScriptEngineFactory factory, Context context) {
    this.factory = factory;
    this.topLevelContext = context;
  }

  public Context.Globals getApartment() {
    return topLevelContext.getGlobals();
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
  public Object eval(Reader reader, Bindings n) throws ScriptException {
    throw new UnsupportedOperationException();
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
  public Object eval(String script, ScriptContext scriptContext)
      throws ScriptException {
    SEXP source = RParser.parseSource(script + "\n");
    return eval(unwrapContext(scriptContext), source);
  }

  @Override
  public Object eval(Reader reader) throws ScriptException {
    return eval(reader, topLevelContext);
  }
  
  @Override
  public Object eval(final Reader reader, ScriptContext scriptContext)
      throws ScriptException {
    return eval(reader, unwrapContext(scriptContext));
  }

  private Object eval(Reader reader, Context context) throws ScriptException {
    SEXP source;
    try {
      // terminate with '\n'
      InputSupplier<Reader> terminated = CharStreams.join(
          newReaderSupplier(reader),
          CharStreams.newReaderSupplier("\n"));
      source = RParser.parseSource(terminated.getInput());
    } catch (IOException e) {
      throw new ScriptException(e);
    }
    return eval(context, source);
  }
  
  private Object eval(Context context, SEXP source) {
    return context.evaluate( source, context.getEnvironment());
  }

  public void eval(File file) throws IOException, ScriptException {
    InputStreamReader reader = new InputStreamReader(
        new FileInputStream(file));
    eval(reader);
    reader.close();
  }
  
  private InputSupplier<Reader> newReaderSupplier(final Reader reader) {
    return new InputSupplier<Reader>() {
      @Override
      public Reader getInput() throws IOException {
        return reader;
      }      
    };
  }

  private Context unwrapContext(ScriptContext scriptContext) {
    return ((RenjinScriptContext)scriptContext).getContext();
  }

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
   
    Function function = topLevelContext.getEnvironment().findFunction(Symbol.get(name));
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
      element = ((Environment)thiz).getVariable(name);
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
    SEXP warnings = topLevelContext.getEnvironment().getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
        topLevelContext.getEnvironment().getBaseEnvironment());
    }

    topLevelContext.getEnvironment().getBaseEnvironment().remove(Warning.LAST_WARNING);
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
