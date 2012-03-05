package r.compiler.ir.tac.expressions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import r.base.Primitives;
import r.compiler.ir.exception.InvalidSyntaxException;
import r.compiler.ir.tac.IRBodyBuilder;
import r.jvmi.wrapper.WrapperGenerator;
import r.jvmi.wrapper.WrapperRuntime;
import r.lang.BuiltinFunction;
import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.PrimitiveFunction;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Symbols;
import r.lang.exception.EvalException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 
 */
public class PrimitiveCall implements Expression {

  private Symbol name;
  
  /**
   * We need to retain the original FunctionCall, even for 
   * strict primitives, because we many primitives actually
   * require us to search for a "generic" implementation first,
   * which might be a closure. 
   */
  private FunctionCall call;
  
  private final List<Expression> arguments;
  private Method primitiveMethod;
  private String[] argumentNames;
  
  /**
   * Elipses (...) need to be handled specially because they are 
   * actually merged into the argument list
   */
  private boolean elipses;
  
  public PrimitiveCall(FunctionCall call, Symbol name, List<Expression> arguments) {
    super();
    this.call = call;
    this.name = name;
    this.arguments = arguments;
    this.argumentNames = new String[arguments.size()];
    for(int i=0;i!=argumentNames.length;++i) {
      argumentNames[i] = Strings.emptyToNull(call.getArguments().getName(i));
    }
    
    for(Expression argument : arguments) {
      if(argument == Elipses.INSTANCE) {
        elipses = true;
        break;
      }
    }
    
    this.primitiveMethod = findMethod(name, arguments.size());
    
  }
  
  private Method findMethod(Symbol name, int arity) {
    try {
      Class wrapperClass = Class.forName(WrapperGenerator.toFullJavaName(name.getPrintName()));
      return wrapperClass.getMethod("matchAndApply", new Class[] { 
          Context.class,
          Environment.class,
          FunctionCall.class,
          String[].class,
          SEXP[].class
      });
//      for(Method method : wrapperClass.getMethods()) {
//        if(method.getName().equals("doApply") && method.getParameterTypes().length == 
//            arity + 2) {
//          return method;
//        }
//      }
    } catch (ClassNotFoundException e) {
      // probably not yet implemented 
      // but throw an error only at runtime to allow compilation to continue
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    }
    return null;
  }
  
  public PrimitiveCall(FunctionCall call, String name, Expression... arguments) {
    this(call, Symbol.get(name), Lists.newArrayList(arguments));
  }
  
  public Symbol getName() {
    return name;
  }
  
  public List<Expression> getArguments() {
    return arguments;
  }
  
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    if(primitiveMethod == null) {
      throw new UnsupportedOperationException("doApply() method for " + name + " not found");
    }

    try {
      if(elipses) {
        return applyDynamic(context, temps);
      } else {
        return applyStatic(context, temps);
      }
    } catch (InvocationTargetException e) {
      if(e.getCause() instanceof EvalException) {
        throw (EvalException)e.getCause();
      } else {
        throw new EvalException(e.getCause());
      }
    } catch (Exception e) {
      throw new EvalException(e);
    }
  }
  
  private SEXP applyStatic(Context context, Object[] temps) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    // names and argument count are determined at 
    // compile time
    SEXP[] argumentValues = new SEXP[arguments.size()];
    for(int i=0;i!=arguments.size();++i) {
      argumentValues[i] = (SEXP)arguments.get(i).retrieveValue(context, temps);
    }
    return (SEXP) primitiveMethod.invoke(null, context, context.getEnvironment(), 
        call, argumentNames, argumentValues);
  }
  
  private SEXP applyDynamic(Context context, Object[] temps) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    // the function call includes a '...' that needs to be expanded into the 
    // argument list
    PairList extraArgs = (PairList) context.getEnvironment().getVariable(Symbols.ELLIPSES);
    int numArgs = arguments.size() + extraArgs.length() - 1;
    String names[] = new String[numArgs];
    SEXP values[] = new SEXP[numArgs];
    int outArgIndex=0;
    int stdArgIndex=0;
    for(Expression arg : arguments) {
      if(arg == Elipses.INSTANCE) {
        for(PairList.Node node : extraArgs.nodes()) {
          names[outArgIndex] = Strings.emptyToNull(node.getName());
          values[outArgIndex] = ((Promise)node.getValue()).force();
          outArgIndex++;
        }
      } else {
        names[outArgIndex] = argumentNames[stdArgIndex];
        values[outArgIndex] = (SEXP) arguments.get(stdArgIndex).retrieveValue(context, temps);
        outArgIndex++;
        stdArgIndex++;
      }
    }
    return (SEXP) primitiveMethod.invoke(null, context, context.getEnvironment(), 
        call, names, values);
  }
  
  @Override
  public String toString() {
    String statement;
    if(name.getPrintName().equals(">") || name.getPrintName().equals("<")) {
      statement = "primitive< " + name + " >";
    } else {
      statement = "primitive<" + name + ">";
    }
    return statement + "(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    for(Expression operand : arguments) {
      variables.addAll(operand.variables());
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public PrimitiveCall replaceVariable(Variable name, Variable newName) {
    List<Expression> newOps = Lists.newArrayListWithCapacity(arguments.size());
    for(Expression argument : arguments) {
      newOps.add(argument.replaceVariable(name, newName));
    }
    return new PrimitiveCall(call, this.name, newOps);
  }

  @Override
  public List<Expression> getChildren() {
    return arguments;
  }

  @Override
  public void setChild(int i, Expression expr) {
    arguments.set(i, expr);
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitPrimitiveCall(this);
  }

  @Override
  public SEXP getSExpression() {
    return call;
  }  
}
