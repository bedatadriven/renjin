/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.combine;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.renjin.invoke.annotations.*;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

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

    CombinedBuilder builder = inspector.newBuilder().useNames(true);

    // Allocate a new vector with all the elements
    return new Combiner(recursive, builder)
        .add(arguments)
        .build();
  }
  
  @Generic
  @Internal
  public static SEXP unlist(SEXP sexp, boolean recursive, boolean useNames) {
    
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
