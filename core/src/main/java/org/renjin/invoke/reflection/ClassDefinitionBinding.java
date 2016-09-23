/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.invoke.reflection;


import org.renjin.eval.EvalException;
import org.renjin.invoke.ClassBinding;
import org.renjin.sexp.Symbol;

/**
 * Binding for an instance of type {@code java.lang.Class}
 * 
 * <p>Provides bindings <strong>both</strong> for static methods of the class described by 
 * {@code classInstance}, as well as the methods and properties of the actual {@code java.lang.Class} object
 * such as {@link Class#getName()} or {@link Class#hashCode()}</p>
 */
public class ClassDefinitionBinding implements ClassBinding {

  private static final Symbol NEW = Symbol.get("new");

  private Class classInstance;
  private final ClassBindingImpl classBinding;
  private final ClassBindingImpl javaLangClassBinding;

  public ClassDefinitionBinding(Class classInstance, ClassBindingImpl classBinding) {
    this.classInstance = classInstance;
    this.classBinding = classBinding;
    this.javaLangClassBinding = ClassBindingImpl.get(Class.class);
  }

  @Override
  public MemberBinding getMemberBinding(Symbol name) {
    if(name == NEW) {
      return classBinding.getConstructorBinding();
    }

    // First check to see if this symbol matches a static binding 
    // for the class being described, for example java.util.HashMap
    final MemberBinding staticMember = classBinding.getStaticMember(name);
    if(staticMember != null) {
      return staticMember;
    }
    
    final MemberBinding instanceMember = javaLangClassBinding.getMemberBinding(name);
    if(instanceMember != null) {
      return instanceMember;
    }
      
    throw new EvalException("Class %s has no static member named '%s', nor does java.lang.Class have an " +
        "instance member named '%s'",
          classBinding.getBoundClass().getName(), name, name);

  }
}
