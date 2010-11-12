/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang.primitive.binding;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import r.lang.*;
import r.lang.primitive.annotations.ArgumentList;
import r.lang.primitive.annotations.Environment;
import r.lang.primitive.annotations.Evaluate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Wraps a {@code java.lang.reflect.Method} and provides
 * useful introspection methods.
 */
public class PrimitiveMethod {
  private Method method;
  private List<Argument> arguments;
  private List<Argument> formals;

  public PrimitiveMethod(Method method) {
    this.method = method;

    ImmutableList.Builder<Argument> argumentsBuilder = ImmutableList.builder();
    for(int i=0; i!=method.getParameterTypes().length;++i) {
      argumentsBuilder.add(new Argument(method, i));
    }
    this.arguments = argumentsBuilder.build();
    this.formals = ImmutableList.copyOf(Iterables.filter(arguments, new IsFormal()));
  }

  /**
   * @return  true if this overload will handle the call itself, that is, it
   * has a signature of (EnvExp, LangExp)
   */
  public boolean acceptsCall() {
    return arguments.size() == 2 &&
           arguments.get(0).getClazz().equals(EnvExp.class) &&
           arguments.get(1).getClazz().equals(LangExp.class);
  }

  public boolean acceptsArgumentList() {
    return arguments.size() == 1 &&
           arguments.get(0).getClazz() == PairList.class &&
           arguments.get(0).isAnnotatedWith(ArgumentList.class);
  }

  public List<Argument> getArguments() {
    return arguments;
  }

  /**
   * @return true if this method's arguments exactly match the {@code expectedArgumentTypes}
   */
  public boolean argumentListEquals(Class... expectedArgumentTypes) {
    return Arrays.equals(method.getParameterTypes(), expectedArgumentTypes);
  }

  public Class getDeclaringClass() {
    return method.getDeclaringClass();
  }

  /**
   * Invokes the underlying static method
   *
    * @param arguments
   * @param <X>
   * @return
   */
  public <X> X invoke(Object... arguments) {
    try {
      return (X) method.invoke(null, arguments);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Access exception while invoking method:\n" + method.toString(), e);
    } catch (InvocationTargetException e) {
      if(e.getCause() instanceof RuntimeException) {
        // Rethrow Runtime Exceptions
        throw (RuntimeException)e.getCause();
      } else {
        // wrap checked exceptions
        throw new RuntimeException("Exception while invoking method from R:\n" + method.toString(), e);
      }
    } catch(IllegalArgumentException e) {
      throw new RuntimeException("IllegalArgumentException while invoking " + method.toString());
    }
  }

  /**
   * Invokes the method with the given arguments and converts the return value to
   * an R {@code SEXP} and wraps the {@code SEXP} in a {@code EvalResult}.
   */
  public EvalResult invokeAndWrap(Object... arguments) {
    Object result = invoke(arguments);

    if(method.getReturnType() == Void.TYPE) {
      return EvalResult.NON_PRINTING_NULL;

    } else if (result instanceof EvalResult ) {
      return (EvalResult)result;

    } else {
      return new EvalResult( SEXPFactory.fromJava(result) );
    }
  }

  public EvalResult invokeWithContextAndWrap(EnvExp rho, Object[] formals) {
    Object params[] = new Object[arguments.size()];
    int formalIndex = 0;

    for(int i=0;i!=arguments.size();i++) {
      if(arguments.get(i).isEnvironment()) {
        params[i] = rho;
      } else {
        params[i] = formals[formalIndex++];
      }
    }
    return invokeAndWrap(params);
  }


  public Class getReturnType() {
    return method.getReturnType();
  }

  public Object getName() {
    return method.getName();    
  }

  public void appendFriendlySignatureTo(StringBuilder sb) {
      sb.append(method.getName()).append("(");
      boolean needsComma=false;
      for(Argument argument : arguments) {
        if(!argument.isEnvironment()) {
          if(needsComma) {
            sb.append(", ");
          } else {
            needsComma=true;
          }
          sb.append(FriendlyTypesNames.get().format(argument.getClazz()));
        }
      }
      sb.append(")");
  }

  public List<Argument> getFormals() {
    return formals;
  }



  public class Argument {
    private int index;
    private Class clazz;
    private boolean environment = false;
    private boolean evaluated = true;
    private boolean symbol;

    public Argument(Method method, int index) {
      clazz = method.getParameterTypes()[index];
      this.index = index;

      for(Annotation annotation : method.getParameterAnnotations()[index]) {
        if(annotation instanceof Environment) {
          environment = true;

        } else if(annotation instanceof Evaluate) {
          evaluated = ((Evaluate) annotation).value();
        }
      }

      symbol = (clazz == SymbolExp.class);
      if(symbol) {
        evaluated = false;
      }
    }

    public boolean isAssignableFrom(Object value) {
      return clazz.isAssignableFrom(value.getClass());
    }

    public Type getTypeArgument(int typeArgumentIndex) {
      Type argType = method.getGenericParameterTypes()[index];
      if(argType instanceof ParameterizedType) {
        return (Class) ((ParameterizedType) argType).getActualTypeArguments()[typeArgumentIndex];
      }
      return null;
    }

    public boolean isAnnotatedWith(Class<? extends Annotation> annotationClass) {
      for(Annotation annotation : method.getParameterAnnotations()[index]) {
        if(annotation.annotationType() == annotationClass) {
          return true;
        }
      }
      return false;
    }

    public Class getClazz() {
      return clazz;
    }

    public boolean isEnvironment() {
      return environment;
    }

    public boolean isEvaluated() {
      return evaluated;
    }

    public boolean isSymbol() {
      return symbol;
    }
  }

  private static class IsFormal implements Predicate<Argument> {
    @Override
    public boolean apply(Argument input) {
      return !input.isEnvironment();
    }
  }

}
