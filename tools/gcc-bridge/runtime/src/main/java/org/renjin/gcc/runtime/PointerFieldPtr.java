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
package org.renjin.gcc.runtime;

import java.lang.reflect.Field;

public class PointerFieldPtr extends AbstractPtr {

  private final Field field;

  public PointerFieldPtr(Field field) {
    this.field = field;
  }

  public static Ptr addressOf(Class declaringClass, String fieldName) {
    try {
      return new PointerFieldPtr(declaringClass.getField(fieldName));
    } catch(Exception e) {
      throw new Error(e);
    }
  }


  @Override
  public Object getArray() {
    return field;
  }

  @Override
  public final int getOffsetInBytes() {
    return 0;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes == 0) {
      return this;
    } else {
      return new OffsetPtr(this, bytes);
    }
  }

  @Override
  public Ptr getPointer() {
    try {
      return (Ptr)field.get(null);
    } catch (IllegalAccessException e) {
      // Should not be reachable: we compile global variables
      // to public static members
      throw new Error(e);
    }
  }

  @Override
  public void setPointer(Ptr value) {
    try {
      field.set(null, value);
    } catch (IllegalAccessException e) {
      // Should not be reachable: we compile global variables
      // to public static members
      throw new Error(e);
    }
  }

  @Override
  public Ptr getAlignedPointer(int index) {
    if(index == 0) {
      return getPointer();
    } else {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public int getInt() {
    return getPointer().toInt();
  }

  @Override
  public void setInt(int value) {
    setPointer(BytePtr.NULL.pointerPlus(value));
  }

  @Override
  public int getInt(int offset) {
    if(offset == 0) {
      return getInt();
    }
    throw new IndexOutOfBoundsException();
  }


  @Override
  public void setInt(int offset, int intValue) {
    if(offset == 0) {
      setInt(intValue);
    } else {
      throw new IndexOutOfBoundsException();
    }
  }


  @Override
  public int getAlignedInt(int index) {
    if(index == 0) {
      return getInt();
    } else {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public void setAlignedInt(int index, int value) {
    if(index == 0) {
      setInt(value);
    } else {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public byte getByte(int offset) {
    return getByteViaInt(offset);
  }

  @Override
  public void setByte(int offset, byte value) {
    setByteViaInt(offset, value);
  }

  @Override
  public int toInt() {
    return 0;
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PointerFieldPtr)) {
      return false;
    }
    PointerFieldPtr that = (PointerFieldPtr) o;
    return this.field.equals(that.field);
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }
}
