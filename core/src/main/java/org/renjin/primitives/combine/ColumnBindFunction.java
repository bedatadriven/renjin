package org.renjin.primitives.combine;

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Special function to do cbind and use objectnames as columnnames
 */
public class ColumnBindFunction extends SpecialFunction {

  public RowBindFunction() {
    super("cbind");
  }

  //  > a = data.frame(G=c(0,0),H=c(1,1))
//  > b = data.frame(I=c(2,2),J=c(3,3))
//  > cbind(a,b)
//    G H I J
//  1 0 1 2 3
//  2 0 1 2 3
//  > rbind(a,b)
//  Error in match.names(clabs, names(xi)) :
//  names do not match previous names
//  > rbind(t(a),t(b))
//    [,1] [,2]
//  G    0    0
//  H    1    1
//  I    2    2
//  J    3    3
//
//  r1 <- 1:4
//  r2 <- 4:7
//  names(r1) <- c('A','B','C','D')
//  names(r2) <- c('A','B','C','D')
//  rbind(r1,r2)
//     A B C D
//  r1 1 2 3 4
//  r2 4 5 6 7
//
//  s1 <- 1:2
//  s2 <- 3:4
//  names(s1) <- c('A','B')
//  names(s2) <- c('C','D')
//  cbind(s1,s2)
//    s1  s2
//  A  1   3
//  B  2   4
  @Override
  public SEXP apply(@Current Context context, @Current Environment rho, int deparseLevel, @ArgumentList ListVector arguments) {

    SEXP genericResult = tryBindDispatch(context, rho, "cbind", deparseLevel, arguments);
    if (genericResult != null) {
      return genericResult;
    }

    List<ColumnBindFunction.BindArgument> bindArguments = Lists.newArrayList();
    for (NamedValue arg : arguments.namedValues()) {
      Vector argument = EvalException.checkedCast(arg.getValue());
      if (argument.length() > 0) {
        bindArguments.add(new ColumnBindFunction.BindArgument(arg.getName(), argument, false));
      }
    }

    if (bindArguments.isEmpty()) {
      return Null.INSTANCE;
    }

    // establish the number of rows
    // 1. check actual matrices
    int rows = -1;
    int columns = 0;
    for (ColumnBindFunction.BindArgument argument : bindArguments) {
      if (argument.matrix) {
        columns += argument.cols;
        if (rows == -1) {
          rows = argument.rows;
        } else if (rows != argument.rows) {
          throw new EvalException("number of rows of matrices must match");
        }
      } else {
        columns++;
      }
    }

    // if there are no actual matrices, then use the longest vector length as the number of rows
    if (rows == -1) {
      for (ColumnBindFunction.BindArgument argument : bindArguments) {
        if (argument.vector.length() > rows) {
          rows = argument.vector.length();
        }
      }
    }

    // now check that all vectors lengths are multiples of the column length
    for (ColumnBindFunction.BindArgument argument : bindArguments) {
      if (!argument.matrix) {
        if ((rows % argument.vector.length()) != 0) {
          throw new EvalException("number of rows of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    inspector.acceptAll(arguments);
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    ColumnBindFunction.Matrix2dBuilder builder = new ColumnBindFunction.Matrix2dBuilder(vectorBuilder, rows, columns);
    for (ColumnBindFunction.BindArgument argument : bindArguments) {
      for (int j = 0; j != argument.cols; ++j) {
        for (int i = 0; i != rows; ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = Null.INSTANCE;
    StringVector.Builder colNames = new StringVector.Builder();

    boolean hasColNames = false;

    for (ColumnBindFunction.BindArgument argument : bindArguments) {
      if (argument.rowNames.length() == rows) {
        rowNames = argument.rowNames;
        break;
      }
    }
    for (ColumnBindFunction.BindArgument argument : bindArguments) {
      if (argument.colNames != Null.INSTANCE) {
        hasColNames = true;
        for (int i = 0; i != argument.cols; ++i) {
          colNames.add(argument.colNames.getElementAsString(i));
        }
      } else if (argument.argName != null && !argument.matrix) {
        colNames.add(argument.argName);
        hasColNames = true;
      } else {
        for (int i = 0; i != argument.cols; ++i) {
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

    public void addFrom(ColumnBindFunction.BindArgument argument, int rowIndex, int colIndex) {
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