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
package org.renjin.gcc.codegen;

import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.*;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.collect.ImmutableList;

import java.util.List;


public class WrapperType {

  public static final WrapperType OBJECT_PTR = new WrapperType(ObjectPtr.class);

  private static final List<WrapperType> TYPES = ImmutableList.of(
      new WrapperType(BytePtr.class),
      new WrapperType(IntPtr.class),
      new WrapperType(ShortPtr.class),
      new WrapperType(LongPtr.class),
      new WrapperType(BooleanPtr.class),
      new WrapperType(CharPtr.class),
      new WrapperType(DoublePtr.class),
      new WrapperType(FloatPtr.class),
      new WrapperType(ObjectPtr.class));
  


  /**
   * The internal class name of the {@code Ptr} class
   */
  private Type wrapperType;

  private final Type arrayType;
  
  private final Type baseType;

  public WrapperType(Class<? extends Ptr> wrapperClass) {
    try {

      Class<?> arrayClass = wrapperClass.getField("array").getType();

      this.wrapperType = Type.getType(wrapperClass);
      this.arrayType = Type.getType(arrayClass);
      this.baseType = Type.getType(arrayClass.getComponentType());
      
    } catch (Exception e) {
      throw new IllegalArgumentException(wrapperClass.getName());
    }
  }

  public Type getWrapperType() {
    return wrapperType;
  }

  public Type getArrayType() {
    return arrayType;
  }

  public Type getBaseType() {
    return baseType;
  }


  public GimpleType getGimpleType() {
    return new GimplePointerType(GimplePrimitiveType.fromJvmType(baseType));
  }


  public static boolean is(Type type) {
    for (WrapperType wrapperType : TYPES) {
      if (wrapperType.getWrapperType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  public static boolean is(Class<?> aClass) {
    return is(Type.getType(aClass));
  }

  public static WrapperType valueOf(Type type) {
    for (WrapperType wrapperType : TYPES) {
      if (wrapperType.getWrapperType().equals(type)) {
        return wrapperType;
      }
    }
    throw new IllegalArgumentException(type.toString());
  }

  @Override
  public String toString() {
    return wrapperType.getInternalName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WrapperType that = (WrapperType) o;

    return wrapperType.equals(that.wrapperType);
  }

  @Override
  public int hashCode() {
    return wrapperType.hashCode();
  }
}
