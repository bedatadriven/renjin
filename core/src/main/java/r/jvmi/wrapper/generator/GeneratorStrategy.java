package r.jvmi.wrapper.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import r.base.Primitives.Entry;
import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.ArgumentException;
import r.jvmi.wrapper.ArgumentIterator;
import r.jvmi.wrapper.GeneratorDefinitionException;
import r.jvmi.wrapper.WrapperRuntime;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.lang.Context;
import r.lang.Environment;
import r.lang.EvalResult;
import r.lang.exception.EvalException;

/**
 * Base class for all the different strategies for generating 
 * wrapper code for a given R function.
 * 
 * 
 * @author alex
 *
 */
public abstract class GeneratorStrategy {

    
  public abstract boolean accept(List<JvmMethod> overloads);
  
 
  public final void generate(WrapperSourceWriter s, Entry entry,
      List<JvmMethod> overloads) {
    
    
    s.writePackage("r.base.primitives");   
    s.writeImport("r.lang.*");
    s.writeImport(WrapperRuntime.class);
    s.writeImport(ArgumentException.class);
    s.writeImport(ArgumentIterator.class);
    s.writeImport(EvalException.class);

    s.writeStaticImport("r.jvmi.wrapper.WrapperRuntime.*");

    s.writeBeginClass();
    s.writeBlankLine();
    s.writeConstructor(entry.name);
    s.writeBlankLine();
    s.writeBeginApplyMethod();
   
    s.writeBeginTry();
    
    generateCall(entry, s, overloads);
    
    s.writeCatch(ArgumentException.class, "e");
    s.writeStatement("throw new EvalException(context, " + argumentErrorMessage(entry, overloads) + ");");
    s.writeCatch(EvalException.class, "e");
    s.writeStatement("e.initContext(context)");
    s.writeStatement("throw e;");    
    s.writeCatch(RuntimeException.class, "e");
    s.writeStatement("throw e;");
    s.writeCatch(Exception.class, "e");
    s.writeStatement("throw new r.lang.exception.EvalException(e);");
    s.writeCloseBlock();
    
    s.writeCloseBlock();

    
    generateOtherCalls(entry, s, overloads);

    
    s.writeCloseBlock();
    s.close();    
  }
  
  protected void generateOtherCalls(Entry entry, WrapperSourceWriter s,
      List<JvmMethod> overloads) {
    
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
  
  protected String noMatchingOverloadErrorMessage(Entry entry, Collection<JvmMethod> overloads) {
    
    int nargs = overloads.iterator().next().countPositionalFormals();
    
    StringBuilder message = new StringBuilder();
    message.append("throw new EvalException(context, ");
    message.append("\"Invalid argument:\\n");
    message.append("\\t").append(entry.name).append("(");
    
    for(int i=0;i<nargs;++i) {
      if(i > 0) {
        message.append(", ");
      }
      message.append("%s");
    }
    message.append(")\\n");
    message.append("\\tExpected:");
    for(JvmMethod method : overloads) {
      message.append("\\n\\t");
      method.appendFriendlySignatureTo(entry.name, message);
    }
    message.append("\"");
    for(int i=0;i<nargs;++i) {
      message.append(", s" + i + ".getTypeName()");
    }
    message.append(")");
    return message.toString();
  }

  protected abstract void generateCall(Entry entry, WrapperSourceWriter s, List<JvmMethod> overloads);

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
  
}
