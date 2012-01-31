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

package r.base;

import java.util.List;

import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.NamedFlag;
import r.jvmi.annotations.Primitive;
import r.lang.AtomicVector;
import r.lang.ComplexVector;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.ExpressionVector;
import r.lang.FunctionCall;
import r.lang.Indexes;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.LogicalVector;
import r.lang.NamedValue;
import r.lang.Null;
import r.lang.PairList;
import r.lang.Promise;
import r.lang.PromisePairList;
import r.lang.SEXP;
import r.lang.SexpVisitor;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
  public static SEXP c(@ArgumentList ListVector arguments,
                       @NamedFlag("recursive") boolean recursive) {

    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(Iterables.transform(arguments.namedValues(), VALUE_OF));

    // Build a new vector with all the elements
    return new Combiner(recursive, inspector.getResult())
        .add(arguments.namedValues())
        .combine();
  }

  public static AtomicVector unlist(AtomicVector vector, boolean recursive, boolean useNames) {
    return vector;
  }

  public static Vector unlist(ListVector vector, boolean recursive, boolean useNames) {
    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(vector);

    return new Combiner(recursive, inspector.getResult())
        .add(vector.namedValues())
        .combine();
  }


  /**
   * Finds the narrowest common type of an expression
   */
  static class Inspector extends SexpVisitor<Vector.Type> {

    private boolean recursive = false;
    private int count = 0;
    private Vector.Type resultType = Null.VECTOR_TYPE;

    /**
     * Visits each element of {@code ListExp}
     */
    Inspector(boolean recursive) {
      this.recursive = recursive;
    }

    @Override
    public void visit(DoubleVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      count += vector.length();
    }

    @Override
    public void visit(IntVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      count += vector.length();
    }

    @Override
    public void visit(LogicalVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      count += vector.length();
    }

    @Override
    public void visit(Null nullExpression) {
      // ignore
    }

    @Override
    public void visit(StringVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      count += vector.length();
    }
    
    @Override
    public void visit(ComplexVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
      count += vector.length();
    }

    @Override
    public void visit(ListVector list) {
      if(recursive) {
        acceptAll(list);
      } else {
        resultType = Vector.Type.widest(resultType, list);
        count += list.length();
      }
    }

    @Override
    public void visit(ExpressionVector vector) {
      visit((ListVector)vector);
    }

    @Override
    protected void unhandled(SEXP exp) {
      resultType = Vector.Type.widest(resultType, ListVector.VECTOR_TYPE);
      count++;
    }

    @Override
    public Vector.Type getResult() {
      return resultType;
    }
    
    public int getCount() {
      return count;
    }
  }

  private static class Combiner {
    private boolean recursive;
    private Vector.Builder vector;

    private StringVector.Builder names = new StringVector.Builder();
    private boolean haveNames = false;

    public Combiner(boolean recursive, Vector.Type resultType) {
      this.recursive = recursive;
      this.vector = resultType.newBuilder();
    }

    public Combiner add(Iterable<NamedValue> list) {
      return add("", list);
    }

    public Combiner add(String parentPrefix, Iterable<? extends NamedValue> list) {
      for(NamedValue namedValue : list) {
        String prefix = combinePrefixes(parentPrefix, Strings.nullToEmpty(namedValue.getName()));
        SEXP value = namedValue.getValue();

        if(recursive && value instanceof ListVector) {
          add(prefix, ((ListVector) value).namedValues());
        } else if(recursive && value instanceof PairList) {
          add(prefix, ((PairList)value).nodes());
        } else {
          for(int i=0;i!=value.length();++i) {
            vector.addFrom(value, i);
            addNameFrom(prefix, value, i);
          }
        }
      }
      return this;
    }

    private void addNameFrom(String prefix, SEXP vector, int index) {
      // The resulting name starts with the argument's
      // tag, if any
      StringBuilder name = new StringBuilder(prefix);

      // if this element has itself a name, then append it
      // to the name, delimiting with a '.' if necessary
      String elementName = vector.getName(index);
      if(!Strings.isNullOrEmpty(elementName)) {
        if(name.length() > 0) {
          name.append('.');
        }
        name.append(elementName);
      } else {

        // if this element has no name of its own, but we're
        // inheriting a name from the argument, AND this vector has
        // multiple values, then we distinguish this element's name
        // from the others in the vector by appending the
        // element's (1-based) index

        if(name.length() > 0 && vector.length() > 1) {
          name.append(index + 1);
        }
      }

      if(name.length() > 0) {
        haveNames = true;
      }

      names.add( name.toString() );
    }

    private String combinePrefixes(String a, String b) {
      assert a != null;
      assert b != null;
      
      if(!a.isEmpty() && !b.isEmpty()) {
        return a + "." + b;
      } else if(!Strings.isNullOrEmpty(a)) {
        return a;
      } else {
        return b;
      }
    }

    public Vector combine() {
      if(haveNames) {
        vector.setAttribute(Symbols.NAMES, names.build());
      }
      return vector.build();
    }
  }

  private static final Function<NamedValue,SEXP> VALUE_OF =
      new Function<NamedValue, SEXP>() {
    @Override
    public SEXP apply(NamedValue input) {
      return input.getValue();
    }
  };


  /**
   * Transpose an array by permuting its dimensions and optionally resizing it.
   * @param source the array to be transposed.
   * @param permutationVector the subscript permutation vector, which must be a permutation of the
   *      integers 1:n, where {@code n} is the number of dimensions of {@code source}.
   * @param resize flag indicating whether the vector should be resized as well as having its elements reordered
   * @return A transposed version of array a, with subscripts permuted as indicated by the array perm.
   * If resize is TRUE, the array is reshaped as well as having
   *  its elements permuted, the dimnames are also permuted; if resize = FALSE then the returned
   * object has the same dimensions as a, and the dimnames are dropped. In each case other attributes
   * are copied from a.
   */
  public static SEXP aperm(Vector source, AtomicVector permutationVector, boolean resize) {
    if(!resize) throw new UnsupportedOperationException("resize=TRUE not yet implemented");

    SEXP dimExp = source.getAttribute(Symbols.DIM);
    EvalException.check(dimExp instanceof IntVector, "invalid first argument, must be an array");
    int dim[] = toIntArray((IntVector) dimExp);

    int permutation[] = toIntArray(permutationVector);
    int permutedDims[] = permute(dim, permutation);

    Vector.Builder newVector = source.newBuilderWithInitialSize(source.length());
    int index[] = new int[dim.length];
    for(int i=0;i!=newVector.length();++i) {
      Indexes.vectorIndexToArrayIndex(i, index, dim);
      index = permute(index, permutation);
      int newIndex = Indexes.arrayIndexToVectorIndex(index, permutedDims);
      newVector.setFrom(newIndex, source, i);
    }

    newVector.setAttribute(Symbols.DIM, new IntVector(permutedDims));

    for(PairList.Node node : source.getAttributes().nodes()) {
      if(node.getTag().equals(Symbols.DIM)) {

      } else if(node.getTag().equals(Symbols.DIMNAMES)) {
        newVector.setAttribute(node.getName(), permute((Vector)node.getValue(), permutation));
      } else {
        newVector.setAttribute(node.getName(), node.getValue());
      }
    }
    return newVector.build();
  }

  private static Vector permute(Vector vector, int[] permutation) {
    Vector.Builder permuted = vector.newBuilderWithInitialSize(vector.length());
    for(int i=0;i!=vector.length();++i) {
      permuted.setFrom(i, vector, permutation[i] - 1);
    }
    return permuted.build();
  }

  private static int[] toIntArray(Vector vector) {
    int values[] = new int[vector.length()];
    for(int i=0;i!=values.length;++i) {
      values[i] = vector.getElementAsInt(i);
    }
    return values;
  }

  private static int[] permute(int values[], int permutation[]) {
    int copy[] = new int[values.length];
    for(int i=0;i!=values.length;++i) {
      copy[i] = values[ permutation[ i ] - 1 ];
    }
    return copy;
  }


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
        bindArguments.add(new BindArgument(argument, true));
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
   * combine by columns. See {@link #rbind(r.lang.ListVector)}
   * @param arguments  the expressions to combined
   * @return  a matrix combining the ... arguments column-wise or row-wise.
   */
  public static SEXP cbind(@Current Context context, @Current Environment rho,
      int deparseLevel, @ArgumentList ListVector arguments) {

    SEXP genericResult = tryBindDispatch(context, rho, "cbind", deparseLevel, arguments);
    if(genericResult != null) {
      return genericResult;
    }
    
    List<BindArgument> bindArguments = Lists.newArrayList();
    for(SEXP arg : arguments) {
      Vector argument = EvalException.checkedCast(arg);
      if(argument.length() > 0) {
        bindArguments.add(new BindArgument(argument, false));
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
    AtomicVector colNames = Null.INSTANCE;

    for(BindArgument argument : bindArguments) {
      if(argument.rowNames.length() == rows) {
        rowNames = argument.rowNames;
        break;
      }
    }

    builder.setDimNames(rowNames, colNames);

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

    public BindArgument(Vector vector, boolean defaultToRows) {
      SEXP dim = vector.getAttribute(Symbols.DIM);
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
    r.lang.Function foundFunction = null;
    
    for(SEXP argument : arguments) {
      Vector classes = (Vector) argument.getAttribute(Symbols.CLASS);
      for(int i=0;i!=classes.length();++i) {
        Symbol methodName = Symbol.get(bindFunctionName + "." + classes.getElementAsString(i));
        r.lang.Function function = rho.findFunction(methodName);
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
   args.add("deparse.level", new Promise(Symbol.get("deparse.level"), new IntVector(deparseLevel)));
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
      return builder.setAttribute(Symbols.DIM, new IntVector(rows,cols))
          .build();
    }
  }

  /**
   * Creates a matrix from the given set of values.
   *
   * @param data an optional data vector.
   * @param nrow the desired number of rows.
   * @param ncol the desired number of columns.
   * @param byRow If FALSE (the default) the matrix is filled by columns, otherwise the matrix is filled by rows.
   * @param dimnames A dimnames attribute for the matrix: NULL or a list of length 2 giving the row and column names
   * respectively. An empty list is treated as NULL, and a list of length one as row names.
   * The list can be named, and the list names will be used as names for the dimensions.
   * @return
   */
  public static SEXP matrix(Vector data, int nrow, int ncol, boolean byRow, Vector dimnames) {
        Vector.Builder result = data.newBuilderWithInitialSize(nrow * ncol);
        int i = 0;

        if(data.length() > 0) {
          if (!byRow) {
              for (int col = 0; col < ncol; ++col) {
                  for (int row = 0; row < nrow; ++row) {
                      int sourceIndex = Indexes.matrixIndexToVectorIndex(row, col, nrow, ncol) % data.length();
                      result.setFrom(i++, data, sourceIndex);
                  }
              }
          } else {
              for (int row = 0; row < nrow; ++row) {
                  for (int col = 0; col < ncol; ++col) {
                      result.setFrom(row + (col * nrow), data, i % data.length());
                      i++;
                  }
              }
          }
        }
        result.setAttribute(Symbols.DIM, new IntVector(nrow, ncol));
        return result.build();
    }

  
  @Primitive
  public static IntVector row(IntVector dims){
    IntVector.Builder data = new IntVector.Builder();
    if(dims.length()!=2){
      throw new EvalException("a matrix-like object is required as argument to 'row/col'");
    }
    int n = dims.getElementAsInt(0);
    int m = dims.getElementAsInt(1);
    for (int i=0;i<n;i++){
      for (int j=0;j<m;j++){
        data.add(i+1);
      }
    }
    IntVector result = (IntVector)matrix(data.build(),n,m,true,null);
    return(result);
  }
  
  @Primitive
  public static IntVector col(IntVector dims){
    IntVector.Builder data = new IntVector.Builder();
    if(dims.length()!=2){
      throw new EvalException("a matrix-like object is required as argument to 'row/col'");
    }
    int n = dims.getElementAsInt(0);
    int m = dims.getElementAsInt(1);
    for (int i=0;i<n;i++){
      for (int j=0;j<m;j++){
        data.add(j+1);
      }
    }
    IntVector result = (IntVector)matrix(data.build(),n,m,true,null);
    return(result);
  }

}
