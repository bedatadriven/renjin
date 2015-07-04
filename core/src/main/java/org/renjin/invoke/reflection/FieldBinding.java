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
