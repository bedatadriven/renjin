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
      throw new EvalException("ExternalPtr is NULL");
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

  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    this.attributes = attributes;
    return this;
  }
}
