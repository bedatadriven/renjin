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

package org.renjin.invoke.model;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.special.Gamma;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.sexp.Logical;
import org.renjin.sexp.Symbol;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
  private boolean dataParallel;
  private boolean passNA;

  private static final Class[] ATOMIC_TYPES = {
      Boolean.TYPE,
      Boolean.class,
      Logical.class,
      Integer.TYPE,
      Integer.class,
      Double.TYPE,
      Double.class,
      Complex.class,
      String.class,
      Byte.class,
      Byte.TYPE };

  public JvmMethod(Method method) {
    this.method = method;

    ImmutableList.Builder<Argument> argumentsBuilder = ImmutableList.builder();
    for(int i=0; i!=method.getParameterTypes().length;++i) {
      argumentsBuilder.add(new Argument(method, i));
    }
    this.arguments = argumentsBuilder.build();
    this.formals = ImmutableList.copyOf(Iterables.filter(arguments, new IsFormal()));

    DataParallel dpAnnotation = method.getAnnotation(DataParallel.class);

    // we special case the methods in java.lang.Math since we can't add
    // annotations to them
    this.dataParallel = dpAnnotation != null ||
      method.getDeclaringClass().equals(Math.class) ||
      method.getDeclaringClass().equals(Gamma.class);

    if(this.dataParallel) {
      this.passNA = dpAnnotation != null && dpAnnotation.passNA();

      // determine which arguments are looped over (recycled in
      // R lingo), and which hold a fixed value
      boolean implicitRecycling = isArgumentRecyclingImplicit();
      for(Argument arg : formals) {
        Recycle recycleAnnotation = arg.getAnnotation(Recycle.class);
        arg.recycle = arg.isAtomicElementType() && (implicitRecycling ||
            (recycleAnnotation == null || recycleAnnotation.value()));
      }
    }

  }

  /**
   * For @DataParallel methods, if none of the arguments are annotated with @Recycle
   * then we consider that we implicitly recycle over all of the arguments
   */
  private boolean isArgumentRecyclingImplicit() {
    for(Argument formal : formals) {
      if(formal.getAnnotation(Recycle.class) != null) {
        return false;
      }
    }
    return true;
  }

  public static List<JvmMethod> findOverloads(Class clazz, String name, String alias) {
    List<JvmMethod> methods = Lists.newArrayList();
    if(clazz != null) {
      for(Method method : clazz.getMethods()) {

        if (isPublic(method.getModifiers()) &&
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
    Builtin builtin = method.getAnnotation(Builtin.class);
    if(builtin != null) {
      return builtin.value();
    }
    Internal internal = method.getAnnotation(Internal.class);
    if(internal != null) {
      return internal.value();
    }
    return "";
  }

  public boolean acceptsArgumentList() {
    for(Argument formal : formals) {
      if(formal.isAnnotatedWith(ArgumentList.class)) {
        return true;
      }
    }
    return false;
  }

  public boolean isDataParallel() {
    return dataParallel;
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


  public PreserveAttributeStyle getPreserveAttributesStyle() {
    DataParallel annotation = method.getAnnotation(DataParallel.class);
    return annotation == null ? PreserveAttributeStyle.STRUCTURAL : annotation.value();
  }
  
  /**
   * @return the name to use for generic dispatch
   */
  public String getGenericName() {
    Builtin primitive = method.getAnnotation(Builtin.class);
    if(primitive != null && primitive.value() != null) {
      return primitive.value();
    }
    return method.getName();
  }
  
  public List<Argument> getAllArguments() {
    return arguments;
  }

  public Method getMethod() {
    return method;
  }
  
  public Class getDeclaringClass() {
    return method.getDeclaringClass();
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
      if (formal.isAnnotatedWith(ArgumentList.class) ||
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

  public boolean isDeferrable() {
    return isAnnotatedWith(Deferrable.class) || method.getDeclaringClass().equals(Math.class);
  }

  public boolean isPassNA() {
    return passNA;
  }

  public boolean isInvisible() {
    return isAnnotatedWith(Invisible.class);
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

    public Argument(Method method, int index) {
      clazz = method.getParameterTypes()[index];
      this.index = index;

      for(Annotation annotation : method.getParameterAnnotations()[index]) {
        if(annotation instanceof Current) {
          contextual = true;

        } else if(annotation instanceof Unevaluated) {
          evaluated = false;

        } else if(annotation instanceof NamedFlag) {
          name = ((NamedFlag) annotation).value();

        } else if(annotation instanceof DefaultValue) {
          defaultValue = ((DefaultValue) annotation).value();
        
        } else if(annotation instanceof InvokeAsCharacter) {
          evaluated = true;
        }
      }

      symbol = (clazz == Symbol.class);
      atomicType = isAtomic(clazz);
    }

    public boolean isAnnotatedWith(Class<? extends Annotation> annotationClass) {
      for(Annotation annotation : method.getParameterAnnotations()[index]) {
        if(annotation.annotationType() == annotationClass) {
          return true;
        }
      }
      return false;
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
      for(Annotation annotation : method.getParameterAnnotations()[index]) {
        if(annotation.annotationType() == annotationClass) {
          return (T) annotation;
        }
      }
      return null;
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

    public boolean isVarArg() {
      return isAnnotatedWith(org.renjin.invoke.annotations.ArgumentList.class);
    }

    public boolean isNamedFlag() {
      return isAnnotatedWith(NamedFlag.class);
    }

    public CastStyle getCastStyle() {
      Cast cast = getAnnotation(Cast.class);
      if(cast == null) {
        return CastStyle.IMPLICIT;
      } else {
        return cast.value();
      }
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
}
