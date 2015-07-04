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
import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Implementation of the combine-related functions, including c(), list(), unlist(),
 *  cbind(), rbind(), matrix(), and aperm()
 */
public class Combine {


  /**
   * combines its arguments to form a vector. All arguments are coerced to a common type which is the
   * type of the returned value, and all attributes except names are removed.
   *
   * @param arguments
   * @param recursive
   * @return
   */
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
        .add(arguments.namedValues())
        .build();
  }

  @Internal
  public static AtomicVector unlist(AtomicVector vector, boolean recursive, boolean useNames) {
    return vector;
  }

  @Internal
  public static Vector unlist(ListVector vector, boolean recursive, boolean useNames) {
    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(vector);

    CombinedBuilder builder = inspector.newBuilder().useNames(useNames);

    return new Combiner(recursive, builder)
        .add(vector.namedValues())
        .build();
  }


  private static final Function<NamedValue,SEXP> VALUE_OF =
      new Function<NamedValue, SEXP>() {
    @Override
    public SEXP apply(NamedValue input) {
      return input.getValue();
    }
  };


  //  private static SEXP dispatchBindCall(@ArgumentList ListVector arguments) {
//    /*   The dispatch algorithm is described in the source file (‘.../src/main/bind.c’) as
//     * For each argument we get the list of possible class memberships from the class attribute.
//     * We inspect each class in turn to see if there is an applicable method.
//     * If we find an applicable method we make sure that it is identical to any method
//      *  determined for prior arguments. If it is identical, we proceed, otherwise we immediately drop through to the default code.
//     */
//
//    for(SEXP argument : arguments) {
//
//    }
//
//  }

  /**
   * {@code cbind} and {@code rbind} take a sequence of vector, matrix or data frames arguments and
   * combine by columns or rows, respectively.
   *
   * <p>This is the default method of cbind (rbind), and all the vectors/matrices must be atomic
   * or lists. Expressions are not allowed. Language objects (such as formulae and calls) and
   * pairlists will be coerced to lists: other objects (such as names and external pointers) will be
   * included as elements in a list result. Any classes the inputs might have are discarded
   * (in particular, factors are replaced by their internal codes).
   *
   * <p>If there are several matrix arguments, they must all have the same number of columns (rows)
   * and this will be the number of columns (or rows) of the result. If all the arguments
   * are vectors, the number of columns (rows) in the result is equal to the length of the
   * longest vector. Values in shorter arguments are recycled to achieve this length
   * (with a warning if they are recycled only fractionally).
   *
   * <p>When the arguments consist of a mix of matrices and vectors the number of rows of the result
   *  is determined by the number of columns (rows) of the matrix arguments. Any vectors have their
   * values recycled or subsetted to achieve this length.
   *
   * <p>For cbind (rbind), vectors of zero length (including NULL) are ignored unless the result would have
   * zero rows (columns), for S compatibility. (Zero-extent matrices do not occur in S3 and are not
   * ignored in R.)
   *
   * @param arguments vectors to combine into rows
   * @return  a matrix combining the ... arguments column-wise or row-wise.
   * (Exception: if there are no inputs or all the inputs are NULL, the value is NULL.)
   */
  @Internal
  public static SEXP rbind(@Current Context context, @Current Environment rho, 
      int deparseLevel, @ArgumentList ListVector arguments) {

    SEXP genericResult = tryBindDispatch(context, rho, "rbind", deparseLevel, arguments);
    if(genericResult != null) {
      return genericResult;
    }
    
    List<BindArgument> bindArguments = Lists.newArrayList();
    for(int i=0;i!=arguments.length();++i) {
      Vector argument = EvalException.checkedCast(arguments.getElementAsSEXP(i));
      if(argument.length() != 0) {
        bindArguments.add(new BindArgument(null, argument, true));
      }
    }

    if(bindArguments.isEmpty()) {
      return Null.INSTANCE;
    }
    
    // establish the number of columns
    // 1. check actual matrices
    int columns = -1;
    int rows = 0;
    for(BindArgument argument : bindArguments) {
      if(argument.matrix) {
        rows += argument.rows;
        if(columns == -1) {
          columns = argument.cols;
        } else if(columns != argument.cols) {
          throw new EvalException("number of columns of matrices must match");
        }
      } else {
        rows ++;
      }
    }

    // if there are no actual matrices, then use the longest vector length as the number of columns
    if(columns == -1) {
      for(BindArgument argument : bindArguments) {
        if(argument.vector.length() > columns) {
          columns = argument.vector.length();
        }
      }
    }


    // now check that all vectors lengths are multiples of the column length
    for(BindArgument argument : bindArguments) {
      if(!argument.matrix) {
        if( (columns % argument.vector.length()) != 0) {
          throw new EvalException("number of columns of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    inspector.acceptAll(arguments);
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for(int j=0;j!=columns;++j) {
      for(BindArgument argument : bindArguments) {
        for(int i=0;i!=argument.rows;++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = Null.INSTANCE;
    AtomicVector colNames = Null.INSTANCE;

    for(BindArgument argument : bindArguments) {
      if(argument.colNames.length() == columns) {
        colNames = argument.colNames;
        break;
      }
    }

    builder.setDimNames(rowNames, colNames);

    return builder.build();
  }

  /**
   * Takes a sequence of vector, matrix or data frames arguments and
   * combine by columns. See {@link #rbind(org.renjin.sexp.ListVector)}
   * @param arguments  the expressions to combined
   * @return  a matrix combining the ... arguments column-wise or row-wise.
   */
  @Internal
  public static SEXP cbind(@Current Context context, @Current Environment rho,
      int deparseLevel, @ArgumentList ListVector arguments) {

    SEXP genericResult = tryBindDispatch(context, rho, "cbind", deparseLevel, arguments);
    if(genericResult != null) {
      return genericResult;
    }
    
    List<BindArgument> bindArguments = Lists.newArrayList();
    for(NamedValue arg : arguments.namedValues()) {
      Vector argument = EvalException.checkedCast(arg.getValue());
      if(argument.length() > 0) {
        bindArguments.add(new BindArgument(arg.getName(), argument, false));
      }
    }

    if(bindArguments.isEmpty()) {
      return Null.INSTANCE;
    }
    
    // establish the number of rows
    // 1. check actual matrices
    int rows = -1;
    int columns = 0;
    for(BindArgument argument : bindArguments) {
      if(argument.matrix) {
        columns += argument.cols;
        if(rows == -1) {
          rows = argument.rows;
        } else if(rows != argument.rows) {
          throw new EvalException("number of rows of matrices must match");
        }
      } else {
        columns ++;
      }
    }

    // if there are no actual matrices, then use the longest vector length as the number of rows
    if(rows == -1) {
      for(BindArgument argument : bindArguments) {
        if(argument.vector.length() > rows) {
          rows = argument.vector.length();
        }
      }
    }

    // now check that all vectors lengths are multiples of the column length
    for(BindArgument argument : bindArguments) {
      if(!argument.matrix) {
        if((rows % argument.vector.length()) != 0) {
          throw new EvalException("number of rows of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    inspector.acceptAll(arguments);
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for(BindArgument argument : bindArguments) {
      for(int j=0;j!=argument.cols;++j) {
        for(int i=0;i!=rows;++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = Null.INSTANCE;
    StringVector.Builder colNames = new StringVector.Builder();

    boolean hasColNames = false;
    
    for(BindArgument argument : bindArguments) {
      if(argument.rowNames.length() == rows) {
        rowNames = argument.rowNames;
        break;
      }
    }
    for(BindArgument argument : bindArguments) {
      if(argument.colNames != Null.INSTANCE) {
        hasColNames = true;
        for(int i=0; i!=argument.cols;++i) {
          colNames.add(argument.colNames.getElementAsString(i));
        }
      } else if(argument.argName!=null && !argument.matrix) {
        colNames.add(argument.argName);
        hasColNames = true;
      } else {
        for(int i=0; i!=argument.cols;++i) {
          colNames.add("");
        }        
      }
    }

    builder.setDimNames(rowNames, 
        hasColNames ? colNames.build() : Null.INSTANCE);

    return builder.build();
  }

  private static class BindArgument {
    private final Vector vector;
    private final int rows;
    private final int cols;

    private AtomicVector rowNames = Null.INSTANCE;
    private AtomicVector colNames = Null.INSTANCE;

    /**
     * True if the argument is an actual matrix
     */
    private final boolean matrix;
    private String argName;

    public BindArgument(String argName, Vector vector, boolean defaultToRows) {
      this.argName = argName;
      SEXP dim = vector.getAttributes().getDim();
      this.vector = vector;
      if(dim == Null.INSTANCE || dim.length() != 2) {
        if(defaultToRows) {
          rows = 1;
          cols = vector.length();
          colNames = vector.getNames();
        } else {
          cols = 1;
          rows = vector.length();
          rowNames = vector.getNames();
        }
        matrix = false;

      } else {
        AtomicVector dimVector = (AtomicVector) dim;
        rows = dimVector.getElementAsInt(0);
        cols = dimVector.getElementAsInt(1);
        Vector dimNames = (Vector) this.vector.getAttribute(Symbols.DIMNAMES);
        if(dimNames instanceof ListVector && dimNames.length() == 2) {
          rowNames = dimNames.getElementAsSEXP(0);
          colNames = dimNames.getElementAsSEXP(1);
        }
        
        matrix = true;
      }
    }
  }
  
  /**
   *    The method dispatching is _not_ done via ‘UseMethod()’, but by
     C-internal dispatching.  Therefore there is no need for, e.g.,
     ‘rbind.default’.

     <p>The dispatch algorithm is described in the source file
     (‘.../src/main/bind.c’) as

    <ol>
     <li>For each argument we get the list of possible class
          memberships from the class attribute.</li>

       <li>We inspect each class in turn to see if there is an
          applicable method.</li>

       <li>If we find an applicable method we make sure that it is
          identical to any method determined for prior arguments.  If
          it is identical, we proceed, otherwise we immediately drop
          through to the default code.</li>
      </ol>
    
   * @param functionName
   * @param arguments
   * @return
   */
  private static SEXP tryBindDispatch(Context context, Environment rho, 
      String bindFunctionName, int deparseLevel, ListVector arguments) {
    
    Symbol foundMethod = null;
    org.renjin.sexp.Function foundFunction = null;
    
    for(SEXP argument : arguments) {
      Vector classes = (Vector) argument.getAttribute(Symbols.CLASS);
      for(int i=0;i!=classes.length();++i) {
        Symbol methodName = Symbol.get(bindFunctionName + "." + classes.getElementAsString(i));
        org.renjin.sexp.Function function = rho.findFunction(context, methodName);
        if(function != null) {
          if(foundMethod != null && methodName != foundMethod) {
            // conflicting overloads,
            // drop into default function
            return null;
          } 
          foundMethod = methodName;
          foundFunction = function;
        }
      }
    }
    
    if(foundFunction == null) {
      // no methods found, drop thru to default
      return null;
    }
    
    // build a new FunctionCall object and apply
   PairList.Builder args = new PairList.Builder();
   args.add("deparse.level", new Promise(Symbol.get("deparse.level"), new IntArrayVector(deparseLevel)));
   args.addAll(arguments);
   
   FunctionCall call = new FunctionCall(Symbol.get(bindFunctionName), args.build());
   return foundFunction.apply(context, rho, call, call.getArguments());
  }
  
  /**
   * Builds a two-dimensional matrix using an underlying {@link Vector.Builder}
   */
  private static class Matrix2dBuilder {
    private final Vector.Builder builder;
    private final int rows;
    private final int cols;
    private int count = 0;

    private Matrix2dBuilder(Vector.Builder builder, int rows, int cols) {
      this.builder = builder;
      this.rows = rows;
      this.cols = cols;
    }

    public void addFrom(BindArgument argument, int rowIndex, int colIndex) {
      int recycledColIndex = colIndex % argument.cols;
      int recycledRowIndex = rowIndex % argument.rows;
      builder.setFrom(count, argument.vector, recycledColIndex * argument.rows + recycledRowIndex);
      count++;
    }

    public void setDimNames(AtomicVector rowNames, AtomicVector colNames) {
      if(rowNames.length() != 0 || colNames.length() != 0) {
        builder.setAttribute(Symbols.DIMNAMES, new ListVector(rowNames, colNames));
      }
    }

    public Vector build() {
      return builder.setAttribute(Symbols.DIM, new IntArrayVector(rows,cols))
          .build();
    }
  }
}
