package org.renjin.script;

import com.google.common.io.CharSource;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.invoke.reflection.converters.RuntimeConverter;
import org.renjin.parser.RParser;
import org.renjin.primitives.Warning;
import org.renjin.primitives.special.BreakException;
import org.renjin.primitives.special.NextException;
import org.renjin.sexp.*;

import javax.script.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RenjinScriptEngine implements ScriptEngine, Invocable {

  private final RenjinScriptEngineFactory factory;
  private final Context topLevelContext;
  
  RenjinScriptEngine(RenjinScriptEngineFactory factory, Session session) {
    super();
    this.factory = factory;
    this.topLevelContext = session.getTopLevelContext();
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
    return topLevelContext.getEnvironment().getVariable(Symbol.get(key));
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
      CharSource terminated = CharSource.concat(
          newReaderSupplier(reader),
         CharSource.wrap("\n"));
      source = RParser.parseSource(terminated);
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
