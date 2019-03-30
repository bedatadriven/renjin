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
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class LazyLoadFrameBuilder {

  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;

  private File outputDir;

  private Context context;

  private Predicate<SEXP> filter = (x -> true);
  private Set<String> excludedSymbols = Collections.emptySet();


  public LazyLoadFrameBuilder(Context context) {
    this.context = context;
  }
  
  public LazyLoadFrameBuilder outputTo(File dir) {
    this.outputDir = dir;
    return this;
  }
  
  public LazyLoadFrameBuilder filter(Predicate<SEXP> filter) {
    this.filter = filter;
    return this;
  }

  public LazyLoadFrameBuilder excludeSymbols(Set<String> excludedSymbols) {
    this.excludedSymbols = excludedSymbols;
    return this;
  }
  
  public void build(Environment env) throws IOException {


    // Now write an index of symbols
    File indexFile = new File(outputDir, "environment");
    try(DataOutputStream indexOut = new DataOutputStream(new FileOutputStream(indexFile))) {

      // mark this format as version 2
      indexOut.writeInt(VERSION_2);

      // Filter the symbols to include
      List<Symbol> symbols = new ArrayList<>();
      for (Symbol symbol : env.getSymbolNames()) {
        if(!excludedSymbols.contains(symbol.getPrintName())) {
          if(!env.isActiveBinding(symbol)) {
            SEXP value = env.getVariableOrThrowIfActivelyBound(symbol);
            if (filter.test(value)) {
              symbols.add(symbol);
            }
          }
        }
      }

      // write out each (large) symbols to a separate resource file.
      // Small values will be serialized directly in the index file
      indexOut.writeInt(symbols.size());

      for (Symbol symbol : symbols) {
        SEXP variable = env.getVariableOrThrowIfActivelyBound(symbol);

        indexOut.writeUTF(symbol.getPrintName());
        byte[] bytes = serializeSymbol(variable);

        if (bytes.length > 1024) {
          indexOut.writeInt(-1);
          Files.write(bytes, new File(outputDir, SerializedPromise.resourceName(symbol.getPrintName())));
        } else {
          indexOut.writeInt(bytes.length);
          indexOut.write(bytes);
        }
      }
    }
  }

  private byte[] serializeSymbol(SEXP value) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos);
    writer.serialize(value);
    baos.close();
    return baos.toByteArray();
  }
}
