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
package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LazyLoadFrame {
  
  private static final int VERSION = 3;

  public static Iterable<NamedValue> load(Context context,
                                          Function<String, InputStream> resourceProvider) throws IOException {

    DataInputStream din = new DataInputStream(resourceProvider.apply("environment"));
    int version = din.readInt();
    if(version == 1) {
      return readVersion1(din);
    }
    if(version == 2) {
      return readVersion2(context, resourceProvider, din);
    }

    if(version != VERSION) {
      throw new IOException("Unsupported version: " + version);
    }

    return readVersion3(context, resourceProvider, din);
  }

  private static Iterable<NamedValue> readVersion1(DataInputStream din) throws IOException {
    int count = din.readInt();
    ListVector.NamedBuilder vector = new ListVector.NamedBuilder(0, count);

    for(int i=0;i!=count;++i) {
      String name = din.readUTF();
      int byteCount = din.readInt();
      byte[] bytes = new byte[byteCount];
      din.readFully(bytes);
      vector.add(name, new SerializedPromise1(bytes));
    }
    din.close();
    return vector.build().namedValues();
  }


  private static Iterable<NamedValue> readVersion2(Context context, Function<String, InputStream> resourceProvider, DataInputStream din) throws IOException {
    int count = din.readInt();
    ListVector.NamedBuilder vector = new ListVector.NamedBuilder(0, count);

    for(int i=0;i!=count;++i) {
      String name = din.readUTF();
      int length = din.readInt();
      if(length < 0) {
        vector.add(name, new SerializedPromise(resourceProvider, name));
      } else {
        byte[] serialized = new byte[length];
        din.readFully(serialized);
        RDataReader reader = new RDataReader(context, new ByteArrayInputStream(serialized));
        vector.add(name, reader.readFile());
      }
    }
    din.close();
    return vector.build().namedValues();
  }


  private static Iterable<NamedValue> readVersion3(Context context, Function<String, InputStream> resourceProvider, DataInputStream din) throws IOException {
    int count = din.readInt();
    ListVector.NamedBuilder vector = new ListVector.NamedBuilder(0, count);

    for(int i=0;i!=count;++i) {
      String name = din.readUTF();
      int length = din.readInt();
      if(length == LazyLoadFrameBuilder3.EXTERNAL_STORAGE) {
        String resourceName = din.readUTF();
        vector.add(name, new SerializedPromise3(resourceProvider, resourceName));

      } else if(length == LazyLoadFrameBuilder3.COMPILED_CLOSURE) {
        vector.add(name, readCompiledClosure(context, din, resourceProvider));

      } else {
        vector.add(name, readInline(context, din, length));
      }
    }
    din.close();
    return vector.build().namedValues();
  }

  private static SEXP readCompiledClosure(Context context, DataInputStream din, Function<String, InputStream> resourceProvider) throws IOException {
    Environment enclosingEnvironment = (Environment) readInline(context, din);
    PairList formals = (PairList)readInline(context, din);
    PairList attributes = (PairList) readInline(context, din);
    String bodyResourceName = din.readUTF();
    SerializedPromise3 bodyPromise = new SerializedPromise3(resourceProvider, bodyResourceName);
    String compiledClassName = din.readUTF().replace('/', '.');
    String compiledMethodName = din.readUTF();

    return new CompiledClosure(enclosingEnvironment, formals, bodyPromise,
        AttributeMap.fromPairList(attributes),
        compiledClassName,
        compiledMethodName);
  }

  private static SEXP readInline(Context context, DataInputStream din) throws IOException {
    int length = din.readInt();
    return readInline(context, din, length);
  }

  private static SEXP readInline(Context context, DataInputStream din, int length) throws IOException {
    byte[] serialized = new byte[length];
    din.readFully(serialized);
    RDataReader reader = new RDataReader(context, new ByteArrayInputStream(serialized));
    return reader.readFile();
  }

}
