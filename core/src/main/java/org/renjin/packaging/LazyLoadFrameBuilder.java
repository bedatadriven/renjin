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
package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.base.Predicates;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;

import java.io.*;

public class LazyLoadFrameBuilder {

  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;

  private File outputDir;

  private Context context;

  private Predicate<NamedValue> filter = Predicates.alwaysTrue();


  public LazyLoadFrameBuilder(Context context) {
    this.context = context;
  }
  
  public LazyLoadFrameBuilder outputTo(File dir) {
    this.outputDir = dir;
    return this;
  }
  
  public LazyLoadFrameBuilder filter(Predicate<NamedValue> filter) {
    this.filter = filter;
    return this;
  }
  
  public void build(Environment env) throws IOException {

    Iterable<NamedValue> toWrite = Iterables.filter(env.namedValues(), filter);


    // Now write an index of symbols
    File indexFile = new File(outputDir, "environment");
    DataOutputStream indexOut = new DataOutputStream(new FileOutputStream(indexFile));

    // mark this format as version 2
    indexOut.writeInt(VERSION_2);

    // write out each (large) symbols to a separate resource file.
    // Small values will be serialized directly in the index file

    indexOut.writeInt(Iterables.size(toWrite));
    for(NamedValue namedValue : toWrite) {

      indexOut.writeUTF(namedValue.getName());
      byte[] bytes = serializeSymbol(namedValue);

      if(bytes.length > 1024) {
        indexOut.writeInt(-1);
        Files.write(bytes, new File(outputDir, SerializedPromise.resourceName(namedValue.getName())));
      } else {
        indexOut.writeInt(bytes.length);
        indexOut.write(bytes);
      }
    }
    indexOut.close();
  }

  private byte[] serializeSymbol(NamedValue namedValue) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos);
    writer.serialize(context, namedValue.getValue());
    baos.close();
    return baos.toByteArray();
  }
}
