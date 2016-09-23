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
package org.renjin.sexp;

import org.renjin.eval.EvalException;
import org.renjin.invoke.ClassBinding;
import org.renjin.invoke.ClassBindings;
import org.renjin.invoke.reflection.MemberBinding;

/**
 * Data type that wraps an external (JVM) pointer
 */
public final class ExternalPtr<T> extends AbstractSEXP {

  private final T instance;
  private final ClassBinding binding;

  public ExternalPtr(T instance, AttributeMap attributes) {
    super(attributes);
    this.instance = instance;
    if(instance == null) {
      this.binding = null;
    } else {
      if(instance instanceof Class) {
        this.binding = ClassBindings.getClassDefinitionBinding((Class) instance);
      } else {
        this.binding = ClassBindings.getClassBinding(instance.getClass());
      }
    }
  }

  public ExternalPtr(T instance) {
    this(instance, AttributeMap.EMPTY);
  }

  @Override
  public String getTypeName() {
    return "externalptr";
  }

  private MemberBinding getMemberBinding(Symbol name) {
    if(binding == null) {
      throw new EvalException("ExternalPtr is NULL for name "+name.getPrintName());
    }
    return binding.getMemberBinding(name);
  }

  public SEXP getMember(Symbol name) {
    return getMemberBinding(name).getValue(instance);
  }
  public void setMember(Symbol name, SEXP value) {
    getMemberBinding(name).setValue(instance, value);
  }

  public T getInstance() {
    return instance;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    this.attributes = attributes;
    return this;
  }
}
