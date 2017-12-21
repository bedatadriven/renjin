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
package org.renjin.primitives.combine;

import org.renjin.invoke.annotations.*;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.sexp.*;

/**
 * Implementation of the combine-related functions, including c(), list(), unlist(),
 *  cbind(), rbind(), matrix(), and aperm()
 */
public class Combine {


  /**
   * combines its arguments to form a vector. All arguments are coerced to a common type which is the
   * type of the returned value, and all attributes except names are removed.
   */
  @Generic
  @Builtin
  public static SEXP c(@ArgumentList ListVector arguments,
                       @NamedFlag("recursive") boolean recursive) {

    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(Iterables.transform(arguments.namedValues(), VALUE_OF));

    if(inspector.getResult() == Null.VECTOR_TYPE) {
      return Null.INSTANCE;
    }

    CombinedBuilder builder = inspector.newBuilder().useNames(true);

    // Allocate a new vector with all the elements
    return new Combiner(recursive, builder)
        .add(arguments)
        .build();
  }
  
  @Generic
  @Internal
  public static SEXP unlist(SEXP sexp, boolean recursive, boolean useNames) {

    if(sexp instanceof FunctionCall) {
      return sexp;
    }

    if(sexp instanceof PairList.Node) {
      sexp = ((PairList.Node) sexp).toVector();
    }

    if(!(sexp instanceof ListVector)) {
      return sexp;
    }
      
    ListVector vector = (ListVector) sexp;
    
    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(vector);
    
    if(inspector.getResult() == Null.VECTOR_TYPE) {
      return Null.INSTANCE;
    }

    CombinedBuilder builder = inspector.newBuilder().useNames(useNames);

    return new Combiner(recursive, builder)
        .add(vector)
        .build();
  }


  private static final Function<NamedValue,SEXP> VALUE_OF =
      new Function<NamedValue, SEXP>() {
    @Override
    public SEXP apply(NamedValue input) {
      return input.getValue();
    }
  };


}
