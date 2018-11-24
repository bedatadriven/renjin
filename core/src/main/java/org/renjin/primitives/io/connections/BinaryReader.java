/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.io.connections;

import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.ByteStreams;
import org.renjin.sexp.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BinaryReader {

  private InputStream in;

  public BinaryReader(RawVector rawVector) {
    this(new ByteArrayInputStream(rawVector.toByteArrayUnsafe()));
  }

  public BinaryReader(InputStream inputStream) {
    this.in = inputStream;
  }

  public Vector readIntVector(int n, int size, boolean signed, boolean swap) throws IOException {
    if(IntVector.isNA(size)) {
      size = 4;
    }

    if(size != 4) {
      throw new EvalException("TODO: size = " + size);
    }

    if(!signed) {
      throw new EvalException("TODO: signed = false");
    }

    if(swap) {
      throw new EvalException("TODO: swap = TRUE");
    }

    int vector[] = new int[n];
    byte buffer[] = new byte[size];

    for (int i = 0; i < n; i++) {
      ByteStreams.readFully(in, buffer);
      vector[i] = buffer[3] << 24 | (buffer[2] & 0xFF) << 16 | (buffer[1] & 0xFF) << 8 | (buffer[0] & 0xFF);
    }

    return IntArrayVector.unsafe(vector);
  }

  public Vector readDoubleVector(int n, int size, boolean swap) throws IOException {
    if(IntVector.isNA(size)) {
      size = 8;
    }
    double[] values;
    switch (size) {
      case 4:
        values = readBinFloatArray(n, swap);
        break;
      case 8:
        values = readBinDoubleArray(n, swap);
        break;
      default:
        throw new EvalException("Unsupported size = " + size + " for numeric vector");
    }

    return DoubleArrayVector.unsafe(values);
  }


  public Vector readComplexVector(int n, int size, boolean swap) throws IOException {
    if(IntVector.isNA(size)) {
      size = 16;
    }
    double[] values;
    switch (size) {
      case 8:
        values = readBinFloatArray(n * 2, swap);
        break;
      case 16:
        values = readBinDoubleArray(n * 2, swap);
        break;
      default:
        throw new EvalException("Unsupported size = " + size + " for complex vector");
    }

    return ComplexArrayVector.unsafe(values);
  }


  private double[] readBinDoubleArray(int n, boolean swap) throws IOException {
    double vector[] = new double[n];
    byte buffer[] = new byte[8];

    for (int i = 0; i < n; i++) {
      ByteStreams.readFully(in, buffer);
      long longValue;
      if(swap) {
        longValue = (((long) buffer[0] << 56) +
            ((long) (buffer[1] & 255) << 48) +
            ((long) (buffer[2] & 255) << 40) +
            ((long) (buffer[3] & 255) << 32) +
            ((long) (buffer[4] & 255) << 24) +
            ((buffer[5] & 255) << 16) +
            ((buffer[6] & 255) << 8) +
            ((buffer[7] & 255) << 0));

      } else {

        longValue = (((long) buffer[7] << 56) +
            ((long) (buffer[6] & 255) << 48) +
            ((long) (buffer[5] & 255) << 40) +
            ((long) (buffer[4] & 255) << 32) +
            ((long) (buffer[3] & 255) << 24) +
            ((buffer[2] & 255) << 16) +
            ((buffer[1] & 255) << 8) +
            ((buffer[0] & 255) << 0));
      }
      vector[i] = Double.longBitsToDouble(longValue);
    }
    return vector;
  }


  private double[] readBinFloatArray(int n, boolean swap) throws IOException {
    double vector[] = new double[n];
    byte buffer[] = new byte[4];

    for (int i = 0; i < n; i++) {
      ByteStreams.readFully(in, buffer);
      int intValue;
      if(swap) {
        intValue = buffer[0] << 24 | (buffer[1] & 0xFF) << 16 | (buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF);
      } else {
        intValue = buffer[3] << 24 | (buffer[2] & 0xFF) << 16 | (buffer[1] & 0xFF) << 8 | (buffer[0] & 0xFF);
      }
      vector[i] = Float.intBitsToFloat(intValue);
    }
    return vector;
  }


  public Vector readCharacterVector(int n, int size, boolean swap) throws IOException {

    StringArrayVector.Builder vector = new StringVector.Builder(0, n);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    for (int i = 0; i < n; i++) {
      buffer.reset();
      while (true) {
        int b = in.read();
        if (b <= 0) {
          break;
        }
        buffer.write(b);
      }
      vector.add(buffer.toString(Charsets.UTF_8.name()));
    }
    return vector.build();
  }

  public Vector readRaw(int n, int size) throws IOException {
    int byteCount = n;
    byte[] buffer = new byte[byteCount];

    int offset = 0;
    while(byteCount > 0) {
      int bytesRead = in.read(buffer, offset, byteCount);
      byteCount -= bytesRead;
      offset += bytesRead;
    }
    return RawVector.unsafe(buffer);
  }
}
