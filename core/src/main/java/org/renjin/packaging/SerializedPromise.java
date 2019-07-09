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
import org.renjin.eval.EvalException;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;

public class SerializedPromise extends Promise {

  private Function<String, InputStream> resourceProvider;
  private String name;

  public SerializedPromise(Function<String, InputStream> resourceProvider, String name) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.resourceProvider = resourceProvider;
    this.name = name;
  }

  @Override
  protected SEXP doEval(Context context) {
    try(RDataReader reader = new RDataReader(context, resourceProvider.apply(resourceName(name)))) {
      return reader.readFile();
    } catch (IOException e) {
      throw new EvalException(e);
    }
  }

  /**
   * Composes a file name for a serialized symbol.
   * 
   * In general, this {symbolName}.RData, but there is a very special case where symbols beginning with "._" will
   * be excluded from the JAR file by the maven-jar-plugin, with no apparent way of overriding.
   * 
   * @param symbolName the name of the symbol to serialize
   * @return the resource name
   */
  public static String resourceName(String symbolName) {


    // maven-jar-plugin ultimately relies on org.codehaus.plexus.util.AbstractScanner
    // which has a default exclude catpattern of "._*"
    // https://github.com/sonatype/plexus-utils/blob/aa6739dc2871e01d6d0ca4564a3a66bcf044c84a/src/main/java/org/codehaus/plexus/util/AbstractScanner.java#L53
    if(symbolName.startsWith("._")) {
      symbolName = "$$" + symbolName;
    }
    
    // A few characters are quite problematic as file names / jar entries
    symbolName = symbolName.replaceAll("/", Matcher.quoteReplacement("$$div$$"));
    
    
    return symbolName + ".RData";
    
  }
}
