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

package r.lang.primitive;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.ArgumentList;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Predicates.not;

public class Combine {

  private static final String RECURSIVE = "recursive";


  public static SEXP combine(@ArgumentList ListVector arguments) {

    // parse arguments
    // we need to look for
    boolean recursive = false;

    Iterator<NamedValue> recursiveOption = Iterators.filter(arguments.namedValues().iterator(), IS_RECURSIVE);
    if(recursiveOption.hasNext()) {
      recursive = recursiveOption.next().getValue().asReal() != 0;
      if(recursiveOption.hasNext()) {
        throw new EvalException("repeated formal argument 'recursive'");
      }
    }

    // Iterate over all the vectors in the argument
    // list to determine which vector type to use
    Inspector inspector = new Inspector(recursive);
    inspector.acceptAll(Iterables.transform(Iterables.filter(arguments.namedValues(), not(IS_RECURSIVE)), VALUE_OF));

    // Build a new vector with all the elements
    return new Combiner(recursive, inspector.getResult())
        .add(Iterables.filter(arguments.namedValues(), Predicates.not(IS_RECURSIVE)))
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
   * Finds the common type of an expression
   */
  static class Inspector extends SexpVisitor<Vector.Type> {

    private boolean recursive = false;
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
    }

    @Override
    public void visit(IntVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
    }

    @Override
    public void visit(LogicalVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
    }

    @Override
    public void visit(Null nullInstance) {
      // ignore
    }

    @Override
    public void visit(StringVector vector) {
      resultType = Vector.Type.widest(resultType, vector);
    }

    @Override
    public void visit(ListVector vector) {
      if(recursive) {
        acceptAll(vector);
      } else {
        resultType = Vector.Type.widest(resultType, vector);
      }
    }

    @Override
    public void visit(ExpressionVector vector) {
      visit((ListVector)vector);
    }

    @Override
    protected void unhandled(SEXP exp) {
      resultType = Vector.Type.widest(resultType, ListVector.VECTOR_TYPE);
    }

    @Override
    public Vector.Type getResult() {
      return resultType;
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
        String prefix = combinePrefixes(parentPrefix, namedValue.getName());
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
      if(!elementName.isEmpty()) {
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
      if(!a.isEmpty() && !b.isEmpty()) {
        return a + "." + b;
      } else if(!a.isEmpty()) {
        return a;
      } else {
        return b;
      }
    }

    public Vector combine() {
      if(haveNames) {
        vector.setAttribute(Attributes.NAMES, names.build());
      }
      return vector.build();
    }
  }

  private static class IsRecursiveArgument implements Predicate<NamedValue> {

    @Override
    public boolean apply(NamedValue input) {
      return input.getName().equals(RECURSIVE);
    }
  }

  private static final IsRecursiveArgument IS_RECURSIVE = new IsRecursiveArgument();

  private static class ValueOf implements Function<NamedValue, SEXP> {
    @Override
    public SEXP apply(NamedValue input) {
      return input.getValue();
    }
  }

  private static final ValueOf VALUE_OF = new ValueOf();


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

  public static SEXP rbind(@ArgumentList ListVector arguments) {



    List<BindArgument> bindArguments = Lists.newArrayList();
    for(SEXP argument : arguments) {
      EvalException.check(argument instanceof Vector, "invalid argument of type '%s'", argument.getTypeName());
      bindArguments.add(new BindArgument((Vector) argument, true));
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

    return builder.build();
  }

  private static class BindArgument {
    private final Vector vector;
    private final int rows;
    private final int cols;
    /**
     * True if the argument is an actual matrix
     */
    private final boolean matrix;

    public BindArgument(Vector vector, boolean defaultToRows) {
      SEXP dim = vector.getAttribute(SymbolExp.DIM);
      this.vector = vector;
      if(dim == Null.INSTANCE || dim.length() != 2) {
        if(defaultToRows) {
          rows = 1;
          cols = vector.length();
        } else {
          cols = 1;
          rows = vector.length();
        }
        matrix = false;

      } else {
        AtomicVector dimVector = (AtomicVector) dim;
        rows = ((AtomicVector) dim).getElementAsInt(0);
        cols = ((AtomicVector) dim).getElementAsInt(1);
        matrix = true;
      }
    }
  }

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
      builder.setFrom(count, argument.vector, recycledColIndex * argument.rows + rowIndex);
      count++;
    }

    public Vector build() {
      return builder.setAttribute(Attributes.DIM, new IntVector(rows,cols))
          .build();
    }
  }
}
