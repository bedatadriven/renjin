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
import org.renjin.invoke.reflection.converters.Converter;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.sexp.SEXP;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldBinding implements MemberBinding {

  private final Field field;
  private final Converter converter;

  public FieldBinding(Field field) {
    this.field = field;
    this.converter = Converters.get(field.getType());
  }

  @Override
  public SEXP getValue(Object instance) {
    try {
      return converter.convertToR(field.get(instance));
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Exception reading value of field " + field, e);
    }
  }

  @Override
  public void setValue(Object instance, SEXP value) {
    if(!Modifier.isFinal(field.getModifiers())) {
      throw new EvalException("The static field '%s' is read-only",
          field.toString());
    }
    try {
      field.set(instance, converter.convertToJava(value));
    } catch (IllegalAccessException e) {
      throw new EvalException("Exception setting field " + field.toString(), e);
    }
  }
}
