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

package org.renjin.primitives.annotations.processor;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.*;
import org.renjin.primitives.special.ControlFlowException;
import org.renjin.sexp.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

/**
 * Wraps a {@code java.lang.reflect.Method} and provides
 * useful introspection methods.
 */
public class JvmMethod implements Comparable<JvmMethod> {
  private Method method;
  private List<Argument> arguments;
  private List<Argument> formals;
  private boolean recycle;

  private static final Class[] ATOMIC_TYPES = {
      Boolean.TYPE,
      Boolean.class,
      Logical.class,
      Integer.TYPE,
      Integer.class,
      Double.TYPE,
      Double.class,
      Complex.class,
      String.class  };

  public JvmMethod(Method method) {
    this.method = method;

    ImmutableList.Builder<Argument> argumentsBuilder = ImmutableList.builder();
    for(int i=0; i!=method.getParameterTypes().length;++i) {
      argumentsBuilder.add(new Argument(method, i));
    }
    this.arguments = argumentsBuilder.build();
    this.formals = ImmutableList.copyOf(Iterables.filter(arguments, new IsFormal()));

    computeRecycling();
  }

  private void computeRecycling() {

    // is recycling disabled for the whole method?
    Recycle methodRecycle = method.getAnnotation(Recycle.class);
    if(methodRecycle != null && !methodRecycle.value()) {
      this.recycle = false;
      return;
    }

    // determine whether Recycling is explicitly defined
    boolean implicitRecycling = true;
    for(Argument formal : formals) {
      if(formal.isAnnotatedWith(Recycle.class)) {
        implicitRecycling = false;
        break;
      }
    }

    // check whether a method with this return type is eligible at all for recycling
    if(! (isAtomic(method.getReturnType()) ||
            (SEXP.class.isAssignableFrom(method.getReturnType()) && !implicitRecycling) )) {
      this.recycle = false;
      return;
    }

    for(Argument formal : formals) {
      formal.recycle = formal.isAnnotatedWith(Recycle.class) ||
          (implicitRecycling && formal.isAtomicElementType() && !formal.hasName());
      if(formal.recycle) {
        this.recycle = true;
      }
    }
  }

  public static List<JvmMethod> findOverloads(Class clazz, String name, String alias) {
    List<JvmMethod> methods = Lists.newArrayList();
    if(clazz != null) {
      for(Method method : clazz.getMethods()) {

        if(isPublic(method.getModifiers()) &&
           isStatic(method.getModifiers()) &&
           ( method.getName().equals(alias) ||
             method.getName().equals(name) ||
             alias(method).equals(name) ) )
        {
          methods.add(new JvmMethod(method));
        }
      }
    }
    validate(methods);
    return methods;
  }

  public static String alias(Method method) {
    Primitive alias = method.getAnnotation(Primitive.class);
    return alias == null ? "" : alias.value();
  }

  /**
   * @return  true if this overload will handle the call itself, that is, it
   * has a signature of (EnvExp, LangExp)
   */
  public boolean acceptsCall() {
    return arguments.size() == 3 &&
           arguments.get(0).getClazz().equals(Context.class) &&
           arguments.get(1).getClazz().equals(Environment.class) &&
           arguments.get(2).getClazz().equals(FunctionCall.class);
  }

  public boolean acceptsArgumentList() {
    for(Argument formal : formals) {
      if(formal.isAnnotatedWith(ArgumentList.class)) {
        return true;
      }
    }
    return false;
  }

  public boolean isRecycle() {
    return recycle;
  }
  
  public boolean isStrict() {
    for(Argument formal : getFormals()) {
      if(!formal.isEvaluated() && !formal.isSymbol()) {
        return false;
      }
    }
    return true;
  }

  public boolean isGeneric() {
    return method.getAnnotation(Generic.class) != null ||
        method.getDeclaringClass().getAnnotation(GroupGeneric.class) != null;
  }
  
  public boolean isGroupGeneric() {
    return method.getDeclaringClass().getAnnotation(GroupGeneric.class) != null ||
        method.getAnnotation(GroupGeneric.class) != null;
  }
  
  public String getGenericGroup() {
    return method.getDeclaringClass().getSimpleName();
  }

  /**
   * Determines whether all attributes should be preserved from the arguments in the return type. 
   * <p>For example:
   * <ul>
   * <li>class(x) == class(-x)</li>
   * <li>attr(x,'foo') == attr(-x, 'foo')</li>
   * 
   * 
   * 
   * @return
   */
  public boolean shouldPreserveAllAttributes() {
    
    return getReturnType().equals(getFormals().get(0).getClass());
  }

  
  public PreserveAttributeStyle getPreserveAttributesStyle() {
    PreserveAttributes annotation = method.getAnnotation(PreserveAttributes.class);
    return annotation == null ? PreserveAttributeStyle.SPECIAL : annotation.value();
  }
  
  /**
   * @return the name to use for generic dispatch
   */
  public String getGenericName() {
    Primitive primitive = method.getAnnotation(Primitive.class);
    if(primitive != null && primitive.value() != null) {
      return primitive.value();
    }
    return method.getName();
  }
  
  public List<Argument> getAllArguments() {
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
      throw new EvalException("Access exception while invoking method:\n" + method.toString(), e);
    } catch (InvocationTargetException e) {
      if(e.getCause() instanceof ControlFlowException) {
        throw (ControlFlowException)e.getCause();
      } else  if(e.getCause() instanceof EvalException) {
        // Rethrow eval Exceptions
        throw (EvalException)e.getCause();
      } else {
        // wrap checked exceptions
        throw new EvalException(e.getCause());
      }
    } catch(IllegalArgumentException e) {
      throw new EvalException("IllegalArgumentException while invoking " + method.toString());
    }
  }

  /**
   * Invokes the method with the given arguments and converts the return value to
   * an R {@code SEXP} and wraps the {@code SEXP} in a {@code EvalResult}.
   */
  public SEXP invokeAndWrap(Context context, Object... arguments) {
    Object result = invoke(arguments);

    if(method.getReturnType() == Void.TYPE) {
      context.setInvisibleFlag();
      return Null.INSTANCE;
    } else {
      return SEXPFactory.fromJava(result);
    }
  }


  public SEXP invokeWithContextAndWrap(Context context, Environment rho, Object[] formals) {
    return invokeAndWrap(context, assembleArgumentListWithContext(context, rho, formals));
  }

  public Object invokeWithContext(Context context, Environment rho, Object[] formals) {
    return invoke( assembleArgumentListWithContext(context, rho, formals ));
  }

  private Object[] assembleArgumentListWithContext(Context context, Environment rho, Object[] formals) {
    Object params[] = new Object[arguments.size()];
    int formalIndex = 0;

    for(int i=0;i!=arguments.size();i++) {
      if(arguments.get(i).isContextual()) {
        Class clazz = arguments.get(i).getClazz();
        if(clazz.equals(Environment.class)) {
          params[i] = rho;
        } else if(clazz.equals(Context.class)) {
          params[i] = context;
        } else {
          throw new UnsupportedOperationException(
              String.format("Cannot inject argument of type '%s' into method %s",
                clazz.getName(), this.toString()));
        }
      } else {
        params[i] = formals[formalIndex++];
      }
    }
    return params;
  }


  public Class getReturnType() {
    return method.getReturnType();
  }
  
  public boolean returnsVoid() {
    return method.getReturnType() == Void.class || method.getReturnType() == Void.TYPE;
  }

  public String getName() {
    return method.getName();
  }
  

  public int countPositionalFormals() {
    return getPositionalFormals().size();
  }

  public List<Argument> getPositionalFormals() {
    List<Argument> list = Lists.newArrayList();
    for(Argument formal : getFormals()) {
      if(formal.isAnnotatedWith(ArgumentList.class) ||
         formal.isAnnotatedWith(NamedFlag.class)) {
        break;
      }
      list.add(formal);
    }
    return list;
  }
  
  public void appendFriendlySignatureTo(StringBuilder sb) {
    appendFriendlySignatureTo(method.getName(), sb);
  }
    
  
  public void appendFriendlySignatureTo(String name, StringBuilder sb) {
      sb.append(name).append("(");
      boolean needsComma=false;
      for(Argument argument : arguments) {
        if(!argument.isContextual()) {
          if(needsComma) {
            sb.append(", ");
          } else {
            needsComma=true;
          }
          if(argument.isAnnotatedWith(ArgumentList.class)) {
            sb.append("...");
          } else {
            sb.append(FriendlyTypesNames.get().format(argument.getClazz()));
            if(!argument.isRecycle() && argument.isAtomicElementType()) {
              sb.append("(1)");
            }
          }
        }
      }
      sb.append(")");
  }

  public List<Argument> getFormals() {
    return formals;
  }

  public boolean isHiddenBy(JvmMethod other) {
    if(formals.size() != other.getFormals().size()) {
      return false;
    }
    for(int i=0;i!=formals.size();++i) {
      if(!formals.get(i).getClazz().isAssignableFrom( other.getFormals().get(i).getClazz() )) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int compareTo(JvmMethod o) {
    if(formals.size() != o.getFormals().size()) {
      return formals.size() - o.getFormals().size();
    }
    if(isHiddenBy(o)) {
      return -1;
    } else if(o.isHiddenBy(this)){
      return +1;
    }
    return 0;
  }

  @Override
  public String toString() {
    return method.toString();
  }

  public boolean isAnnotatedWith(Class<? extends Annotation> annotationClass) {
    return method.isAnnotationPresent(annotationClass);
  }

  public boolean acceptsNA() {
    return method.isAnnotationPresent(AllowNA.class);
  }

  public int getFormalIndexByName(String name) {
    Preconditions.checkNotNull(name);
    for(int i=0;i!=formals.size();++i) {
      if(name.equals(formals.get(i).getName())) {
        return i;
      }
    }
    return -1;
  }

  public class Argument {
    private int index;
    private Class clazz;
    private boolean contextual = false;
    private boolean evaluated = true;
    private boolean symbol;
    private String name;
    public boolean recycle;
    public boolean atomicType;
    public boolean defaultValue;
    
    public ExplicitConverter explicitConverter;

    public Argument(Method method, int index) {
      clazz = method.getParameterTypes()[index];
      this.index = index;

      for(Annotation annotation : method.getParameterAnnotations()[index]) {
        if(annotation instanceof Current) {
          contextual = true;

        } else if(annotation instanceof Evaluate) {
          evaluated = ((Evaluate) annotation).value();

        } else if(annotation instanceof NamedFlag) {
          name = ((NamedFlag) annotation).value();

        } else if(annotation instanceof DefaultValue) {
          defaultValue = ((DefaultValue) annotation).value();
        
        } else if(annotation instanceof InvokeAsCharacter) {
          evaluated = true;
          explicitConverter = new AsCharacterConverter();
        }
      }

      symbol = (clazz == Symbol.class);
      atomicType = isAtomic(clazz);
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

    public boolean isContextual() {
      return contextual;
    }

    public boolean isEvaluated() {
      return evaluated;
    }

    public boolean isSymbol() {
      return symbol;
    }

    public boolean isAtomicElementType() {
      return atomicType;
    }

    public boolean isRecycle() {
      return recycle;
    }

    public boolean hasName() {
      return name != null;
    }

    public String getName() {
      return name;
    }

    public boolean getDefaultValue() {
      return defaultValue;
    }
    
    public int getIndex() {
      return index;
    }
    
    /**
     * 
     * @return then name of the R-language converter function
     * that will first convert provided arguments.
     */
    public ExplicitConverter getExplicitConverter() {
      return explicitConverter;
    }

    /**
     * 
     * @return true if this argument has it's own R-language converter
     * function (like "as.character")
     */
    public boolean hasExplicitConverter() {
      return explicitConverter != null;
    }

    public boolean isVarArg() {
      return isAnnotatedWith(org.renjin.primitives.annotations.ArgumentList.class);
    }

    public boolean isNamedFlag() {
      return isAnnotatedWith(NamedFlag.class);
    }
  }

  private class IsFormal implements Predicate<Argument> {
    @Override
    public boolean apply(Argument input) {
      return !input.isContextual();
    }
  }

  public static void validate(List<JvmMethod> methods) {
    for(int i=0;i!=methods.size(); ++i) {
      for(int j=0;j!=methods.size(); ++j) {
        if(i!=j) {
          JvmMethod x = methods.get(i);
          JvmMethod y = methods.get(j);

          if(x.isHiddenBy(y)) {
            throw new EvalException(formatHiddenMethod(x,y));
          }
        }
      }
    }
  }

  private static String formatHiddenMethod(JvmMethod x, JvmMethod y) {
    StringBuilder sb = new StringBuilder();
    sb.append("Primitive method\n\t");
    x.appendFriendlySignatureTo(sb);
    sb.append("\nis hidden by\n\t");
    y.appendFriendlySignatureTo(sb);
    return sb.append("\n").toString();
  }

  private boolean isAtomic(Class clazz) {
    for(int i=0;i!=ATOMIC_TYPES.length;++i) {
      if(clazz.equals(ATOMIC_TYPES[i])) {
        return true;
      }
    }
    return false;
  }
  
  public interface ExplicitConverter {
    SEXP convert(Context context, Environment rho, SEXP provided);
  }
  
  public class AsCharacterConverter implements ExplicitConverter {

    @Override
    public SEXP convert(Context context, Environment rho, SEXP provided) {
      return WrapperRuntime.invokeAsCharacter(context, rho, provided);
    }
  }


}
