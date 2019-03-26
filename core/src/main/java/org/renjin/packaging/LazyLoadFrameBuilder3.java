/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */
package org.renjin.packaging;

import org.renjin.compiler.aot.AotBuffer;
import org.renjin.compiler.aot.AotHandle;
import org.renjin.compiler.aot.ClosureCompiler;
import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class LazyLoadFrameBuilder3 {

  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;
  private static final int VERSION_3 = 3;

  public static final int EXTERNAL_STORAGE = -1;
  public static final int COMPILED_CLOSURE = -2;

  private File outputDir;

  private final Context context;
  private final AotBuffer buffer;

  private Predicate<SEXP> filter = (x -> true);
  private Set<String> excludedSymbols = Collections.emptySet();

  private Set<String> uniqueNames = new HashSet<>();

  public LazyLoadFrameBuilder3(Context context) {
    this.context = context;
    this.buffer = new AotBuffer("org.renjin.base");
  }
  
  public LazyLoadFrameBuilder3 outputTo(File dir) {
    this.outputDir = dir;
    return this;
  }
  
  public LazyLoadFrameBuilder3 filter(Predicate<SEXP> filter) {
    this.filter = filter;
    return this;
  }

  public LazyLoadFrameBuilder3 excludeSymbols(Set<String> excludedSymbols) {
    this.excludedSymbols = excludedSymbols;
    return this;
  }
  
  public void build(Environment env) throws IOException {


    // Now write an index of symbols
    File indexFile = new File(outputDir, "environment");
    try(DataOutputStream indexOut = new DataOutputStream(new FileOutputStream(indexFile))) {

      // mark this format as version 3
      // which includes compiled closures
      indexOut.writeInt(VERSION_3);

      // Filter the symbols to include
      List<Symbol> symbols = new ArrayList<>();
      for (Symbol symbol : env.getFrame().getSymbols()) {
        if(!excludedSymbols.contains(symbol.getPrintName())) {
          SEXP value = env.getFrame().getVariable(symbol);
          if(filter.test(value)) {
            symbols.add(symbol);
          }
        }
      }

      indexOut.writeInt(symbols.size());

      for (Symbol symbol : symbols) {
        write(indexOut, symbol, env.getFrame().getVariable(symbol));
      }
      buffer.flushTo(outputDir);
    }
  }

  private void write(DataOutputStream indexOut, Symbol symbol, SEXP value) throws IOException {

    indexOut.writeUTF(symbol.getPrintName());

    if(value instanceof Closure) {
      if (compileAndSerializeClosure(indexOut, symbol, (Closure) value)) {
        return;
      }
    }

    byte[] bytes = serializeSymbol(value);

    // write out each (large) symbols to a separate resource file.
    // Small values will be serialized directly in the index file

    if (bytes.length > 1024) {
      indexOut.writeInt(EXTERNAL_STORAGE);
      writeExternalResource(indexOut, symbol, bytes);
    } else {
      writeInlineResource(indexOut, bytes);
    }
  }

  private void writeInlineResource(DataOutputStream indexOut, byte[] bytes) throws IOException {
    indexOut.writeInt(bytes.length);
    indexOut.write(bytes);
  }

  /**
   * Write a large serialized value to an external resource file that can be loaded
   * on demand.
   *
   * @param indexOut the OutputStream for the index file
   * @param symbol the name of the symbol
   * @param bytes byte array containing the serialized symbol
   * @throws IOException
   */
  private void writeExternalResource(DataOutputStream indexOut, Symbol symbol, byte[] bytes) throws IOException {

    String resourceName = ensureUnique(sanitizeResourceName(symbol.getPrintName())) + ".RData";

    indexOut.writeUTF(resourceName);

    Files.write(bytes, new File(outputDir, resourceName));
  }


  private String sanitizeResourceName(String symbolName) {
    // maven-jar-plugin ultimately relies on org.codehaus.plexus.util.AbstractScanner
    // which has a default exclude catpattern of "._*"
    // https://github.com/sonatype/plexus-utils/blob/aa6739dc2871e01d6d0ca4564a3a66bcf044c84a/src/main/java/org/codehaus/plexus/util/AbstractScanner.java#L53
    if(symbolName.startsWith("._")) {
      symbolName = "$$" + symbolName;
    }

    // A few characters are quite problematic as file names / jar entries
    symbolName = symbolName.replaceAll("/", Matcher.quoteReplacement("$$div$$"));
    return symbolName;
  }

  private String ensureUnique(String symbolName) {
    if(uniqueNames.contains(symbolName)) {
      int alias = 1;
      while(uniqueNames.contains(symbolName.toLowerCase() + "." + alias)) {
        alias++;
      }
      symbolName = symbolName + "." + alias;
    }
    return symbolName;
  }

  private boolean compileAndSerializeClosure(DataOutputStream indexOut, Symbol symbol, Closure closure) throws IOException {
    AotHandle handle;
    try {
      ClosureCompiler compiler = new ClosureCompiler(buffer, context, symbol, closure);
      handle = compiler.getHandle();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    // Write out the header
    indexOut.writeInt(COMPILED_CLOSURE);

    // Write out the containing environment and the list of formals inline
    writeInlineResource(indexOut, serializeSymbol(closure.getEnclosingEnvironment()));
    writeInlineResource(indexOut, serializeSymbol(closure.getFormals()));
    writeInlineResource(indexOut, serializeSymbol(closure.getAttributes().asPairList()));

    // Write the body of the function to an external resource file
    writeExternalResource(indexOut, symbol, serializeSymbol(closure.getBody()));

    // Write the name of the compiled class and method
    indexOut.writeUTF(handle.getClassName());
    indexOut.writeUTF(handle.getMethodName());

    return true;
  }

  private byte[] serializeSymbol(SEXP value) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos);
    writer.serialize(value);
    baos.close();
    return baos.toByteArray();
  }
}
