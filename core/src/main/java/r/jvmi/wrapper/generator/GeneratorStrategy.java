package r.jvmi.wrapper.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import r.base.BaseFrame.Entry;
import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.ArgumentException;
import r.jvmi.wrapper.GeneratorDefinitionException;
import r.jvmi.wrapper.WrapperRuntime;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.args.ArgConverterStrategy;
import r.jvmi.wrapper.generator.args.SexpSubclass;
import r.jvmi.wrapper.generator.args.ToScalar;
import r.jvmi.wrapper.generator.args.UnwrapExternalObject;
import r.jvmi.wrapper.generator.args.UsingAsCharacter;
import r.lang.Context;
import r.lang.Environment;
import r.lang.EvalResult;
import r.lang.SEXP;
import r.lang.exception.EvalException;

import com.google.common.collect.Lists;

/**
 * Base class for all the different strategies for generating 
 * wrapper code for a given R function.
 * 
 * 
 * @author alex
 *
 */
public abstract class GeneratorStrategy {

  private List<ArgConverterStrategy> argumentConverters;
  
  public GeneratorStrategy() {
    argumentConverters = Lists.newArrayList();
    argumentConverters.add(new UsingAsCharacter());
    argumentConverters.add(new SexpSubclass());
    argumentConverters.add(new ToScalar());
    argumentConverters.add(new UnwrapExternalObject());
  }
    
  public abstract boolean accept(List<JvmMethod> overloads);
  
 
  public final void generate(WrapperSourceWriter s, Entry entry,
      List<JvmMethod> overloads) {
    
    
    s.writePackage("r.base.primitives");   
    s.writeImport("r.lang.*");
    s.writeImport(WrapperRuntime.class);
    s.writeImport(ArgumentException.class);
    s.writeImport(EvalException.class);

    s.writeStaticImport("r.jvmi.wrapper.WrapperRuntime.*");

    s.writeBeginClass();
    s.writeBlankLine();
    s.writeConstructor(entry.name);
    s.writeBlankLine();
    s.writeBeginApplyMethod();
   
    s.writeBeginTry();
    
    generateCall(s, overloads);
    
    s.writeCatch(ArgumentException.class, "e");
    s.writeStatement("throw new EvalException(" + argumentErrorMessage(entry, overloads) + ");");
    
    s.writeCatch(RuntimeException.class, "e");
    s.writeStatement("throw e;");
    s.writeCatch(Exception.class, "e");
    s.writeStatement("throw new r.lang.exception.EvalException(e);");
    s.writeCloseBlock();
    
    
    s.writeCloseBlock();
    s.writeCloseBlock();
    s.close();    
  }
  
  private String argumentErrorMessage(Entry entry, List<JvmMethod> overloads) {
    StringBuilder message = new StringBuilder();
    message.append("\"Invalid argument. Expected:");
    for(JvmMethod method : overloads) {
      message.append("\\n\\t");
      method.appendFriendlySignatureTo(entry.name, message);
    }
    message.append("\"");
    return message.toString();
  }

  protected abstract void generateCall(WrapperSourceWriter s, List<JvmMethod> overloads);

  protected final String contextualArgumentName(Argument formal) {
    if(formal.getClazz().equals(Context.class)) {
      return "context";
    } else if(formal.getClazz().equals(Environment.class)) {
      return "rho"; 
    } else {
      throw new RuntimeException("Invalid contextual argument type: " + formal.getClazz());
    }
  }

  protected static class ArgumentList {
    private StringBuilder sb = new StringBuilder();
    
    public ArgumentList(String... args) {
      for(String arg : args) {
        add(arg);
      }
    }
    
    public void add(String name) {
      if(sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(name);
    }
    @Override
    public String toString() {
      return sb.toString();
    }
  }
    
  protected final String argConversionStatement(JvmMethod.Argument formal, String tempLocal) {
    String exp;
    if(formal.isEvaluated()) {
      exp = "argumentValue(args).evalToExp(context, rho)";
    } else {
      exp = "argumentValue(args)";
    }
    
    if(formal.getClazz().equals(SEXP.class)) {
      return tempLocal + " = " + exp + ";"; 
    } else {
      return findArgConverterStrategy(formal).conversionStatement(formal, tempLocal, exp);
    }
  }
  
  private ArgConverterStrategy findArgConverterStrategy(JvmMethod.Argument formal) {
    for(ArgConverterStrategy strategy : argumentConverters) {
      if(strategy.accept(formal)) {
        return strategy;
      }
    }
    throw new GeneratorDefinitionException("Could not find a strategy for converting to argument " + formal.getIndex() + " of type " + formal.getClazz());
  }
  
  protected final String callStatement(JvmMethod method, ArgumentList argumentList) {
    StringBuilder call = new StringBuilder();
    call.append(method.getDeclaringClass().getName()).append(".")
      .append(method.getName())
      .append("(")
      .append(argumentList.toString())
      .append(")");
    
    return handleReturn(method, call.toString());
  }
  
  protected final String handleReturn(JvmMethod method, String execution) {
    if(method.returnsVoid()) {
      return execution + ";";
      
    } else if(method.getReturnType().equals(EvalResult.class)) {
      return "return " + execution + ";";
    
    } else {
      verifyWeHaveWrapResult(method);
      return "return wrapResult(" + execution + ");";    
    }
  }
  
  private void verifyWeHaveWrapResult(JvmMethod method) {
    for(Method runtimeMethod : WrapperRuntime.class.getMethods()) {
      if(runtimeMethod.getName().equals("wrapResult") &&
         runtimeMethod.getParameterTypes().length == 1 &&
         (runtimeMethod.getModifiers() & Modifier.STATIC) != 0 &&
         runtimeMethod.getParameterTypes()[0].isAssignableFrom(method.getReturnType())) {
        return;
      }
    }
    throw new GeneratorDefinitionException("Do not have a wrapper for return type " + method.getReturnType().getName());
  }
  
  protected final String toJava(Class<?> clazz) {
    StringBuilder sb = new StringBuilder();
    sb.append(clazz.getPackage().getName());
    if(clazz.getEnclosingClass() != null) {
      sb.append(".");
      sb.append(clazz.getEnclosingClass().getSimpleName());
    }
    sb.append(".");
    sb.append(clazz.getSimpleName());
    return sb.toString();
  }
  
}
