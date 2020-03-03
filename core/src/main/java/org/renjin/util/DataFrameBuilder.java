/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.util;

import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.invoke.reflection.MemberBinding;
import org.renjin.invoke.reflection.PropertyBinding;
import org.renjin.invoke.reflection.converters.*;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper functions to create R Data Types
 */
public class DataFrameBuilder {
  
  private abstract static class Column {
    protected final PropertyBinding propertyBinding;
    protected final AtomicVectorConverter converter;
    
    public Column(PropertyBinding propertyBinding, int length) {
      this.propertyBinding = propertyBinding;
      this.converter = (AtomicVectorConverter) propertyBinding.getConverter();
      init(length);
    }
    
    public String getName() {
      return propertyBinding.getName().getPrintName();
    }
    
    public abstract void init(int length);
    
    public abstract void add(Object instance);
    
    public abstract Vector build();
  }
  
  private static class StringColumn extends Column {

    private StringArrayVector.Builder builder;
    
    public StringColumn(PropertyBinding propertyBinding, int length) {
      super(propertyBinding, length);
    }

    @Override
    public void init(int length) {
      builder = new StringArrayVector.Builder(0, length);
    }

    @Override
    public void add(Object instance) {
      builder.add((String)propertyBinding.getRawValue(instance));
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }
  
  private static class NumberColumn extends Column {

    private AtomicVector.Builder builder;

    public NumberColumn(PropertyBinding propertyBinding, int length) {
      super(propertyBinding, length);
    }

    @Override
    public void init(int length) {
      this.builder = converter.getVectorType().newBuilderWithInitialCapacity(length);
    }

    @Override
    public void add(Object instance) {
      Number value = (Number)propertyBinding.getRawValue(instance);
      if(value == null) {
        builder.addNA();
      } else {
        builder.add(value);
      }
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }
  
  private static class EnumColumn extends Column {
    private StringVector.Builder builder;

    public EnumColumn(PropertyBinding propertyBinding, int length) {
      super(propertyBinding, length);
    }

    @Override
    public void init(int length) {
      builder = new StringArrayVector.Builder(0, length);
    }

    @Override
    public void add(Object instance) {
      Enum rawValue = (Enum) propertyBinding.getRawValue(instance);
      builder.add(rawValue.name());
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }

  private static class BooleanColumn extends Column {

    private LogicalArrayVector.Builder builder;

    public BooleanColumn(PropertyBinding propertyBinding, int length) {
      super(propertyBinding, length);
    }

    @Override
    public void init(int length) {
      this.builder = new LogicalArrayVector.Builder(0, length);
    }

    @Override
    public void add(Object instance) {
      Boolean value = (Boolean) propertyBinding.getRawValue(instance);
      if(value == null) {
        builder.addNA();
      } else {
        builder.add(value);
      }
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }
  
  public static ListVector build(ExternalPtr externalPointer) {
    if(externalPointer.getInstance() instanceof Collection) {
      Collection collection = (Collection) externalPointer.getInstance();
      if(collection.isEmpty()) {
        throw new EvalException("Cannot create data.frame from an empty list.");
      }
      Class<?> itemClass = collection.iterator().next().getClass();
      return build(itemClass, collection);
    }
    throw new EvalException("Unsupported class: " + externalPointer.getInstance().getClass().getName());
  }
  
  /**
   * Converts a collection of Java Beans to an R Data Frame. 
   * @param beanClass the class of the objects
   * @param rows a collection of instances
   * @param <T>
   * @return a ListVector
   */
  public static <T> ListVector build(Class<T> beanClass, Collection<T> rows) {

    int numRows = rows.size();
    
    List<Column> columns = new ArrayList<>();
    
    ClassBindingImpl classBinding = ClassBindingImpl.get(beanClass);
    for (Symbol symbol : classBinding.getMembers()) {
      MemberBinding memberBinding = classBinding.getMemberBinding(symbol);
      if(memberBinding instanceof PropertyBinding) {
        PropertyBinding property = (PropertyBinding) memberBinding;
        Converter converter = property.getConverter();
        if(converter instanceof StringConverter) {
          columns.add(new StringColumn(property, numRows));
        } else if(converter instanceof BooleanConverter) {
          columns.add(new BooleanColumn(property, numRows));
        } else if(converter instanceof EnumConverter) {
          columns.add(new EnumColumn(property, numRows));
        } else if(converter instanceof AtomicVectorConverter) {
          columns.add(new NumberColumn(property, numRows));
        } 
      }
    }

    for (T row : rows) {
      for (Column column : columns) {
        column.add(row);
      }
    }
    
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    for (Column column : columns) {
      list.add(column.getName(), column.build());
    }
    list.setAttribute(Symbols.ROW_NAMES, new IntSequence(1, 1, numRows));
    list.setAttribute(Symbols.CLASS, StringArrayVector.valueOf("data.frame"));
    
    return list.build();
  }
  
}
