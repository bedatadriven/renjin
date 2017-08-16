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
package org.renjin.gcc.runtime;


public interface Ptr {

  @Deprecated
  Object getArray();

  @Deprecated
  int getOffset();
  
  Ptr realloc(int newSizeInBytes);

  /**
   * Returns a new Pointer with the given byte count offset.
   */
  Ptr pointerPlus(int bytes);


  /**
   *
   * @return the value at the beginning of the pointer region as a byte
   */
  byte getByte();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the byte at the given offset from the beginning of the pointer region.
   */
  byte getByte(int offset);


  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setByte(byte value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setByte(int offset, byte value);


  short getShort();

  short getShort(int offset);

  void setShort(short value);

  void setShort(int offset, short value);

  /**
   *
   * @return the value at the beginning of the pointer region as a char
   */
  char getChar();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the char at the given offset from the beginning of the pointer region.
   */
  char getChar(int offset);

  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setChar(char value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setChar(int offset, char value);


  /**
   *
   * @return the value at the beginning of the pointer region as a double
   */
  double getDouble();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the float at the given offset from the beginning of the pointer region.
   */
  double getDouble(int offset);

  /**
   * @param index the index of the 8-byte double, from the beginning of the pointer.
   */
  double getAlignedDouble(int index);

  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setDouble(double value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setDouble(int offset, double value);


  /**
   *
   * @return the value at the beginning of the pointer region as a float
   */
  float getFloat();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the float at the given offset from the beginning of the pointer region.
   */
  float getFloat(int offset);

  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setFloat(float value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setFloat(int offset, float value);


  /**
   *
   * @return the value at the beginning of the pointer region as a float
   */
  int getInt();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the float at the given offset from the beginning of the pointer region.
   */
  int getInt(int offset);

  /**
   *
   * @param index the index of the 4-byte integer, from the beginning of the pointer.
   */
  int getIntAligned(int index);

  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setInt(int value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setInt(int offset, int value);



  /**
   *
   * @return the value at the beginning of the pointer region as a long
   */
  long getLong();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the float at the given offset from the beginning of the pointer region.
   */
  long getLong(int offset);

  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setLong(long value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setLong(int offset, long value);



  /**
   *
   * @return the value at the beginning of the pointer region as a {@code Pointer}
   */
  Ptr getPointer();

  /**
   *
   * @param offset the offset from the beginning of the pointer, in bytes.
   * @return the Pointer at the given offset from the beginning of the pointer region.
   */
  Ptr getPointer(int offset);

  /**
   * Sets the given {@code value} at the beginning of the pointer.
   */
  void setPointer(Ptr value);


  /**
   * Sets the given {@code value} at the offset from the beginning of the pointer.
   * @param offset  the offset from the beginning of the pointer, in bytes.
   */
  void setPointer(int offset, Ptr value);


  /**
   * Cast this "pointer" to a 32-bit integer.
   */
  int toInt();


}
