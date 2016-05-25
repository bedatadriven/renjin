package org.renjin.primitives.combine;

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Special function to do rbind and use objectnames as rownames
 */
public class RowBindFunction extends SpecialFunction {

  public RowBindFunction() {
    super("rbind");
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

  @Override
  public SEXP apply(@Current Context context, @Current Environment rho, int deparseLevel, @ArgumentList ListVector arguments) {

    SEXP genericResult = tryBindDispatch(context, rho, "rbind", deparseLevel, arguments);
    if (genericResult != null) {
      return genericResult;
    }

    List<Combine.BindArgument> bindArguments = Lists.newArrayList();
    for (int i = 0; i != arguments.length(); ++i) {
      Vector argument = EvalException.checkedCast(arguments.getElementAsSEXP(i));
      if (argument.length() != 0) {
        bindArguments.add(new Combine.BindArgument(null, argument, true));
      }
    }

    if (bindArguments.isEmpty()) {
      return Null.INSTANCE;
    }

    // establish the number of columns
    // 1. check actual matrices
    int columns = -1;
    int rows = 0;
    for (Combine.BindArgument argument : bindArguments) {
      if (argument.matrix) {
        rows += argument.rows;
        if (columns == -1) {
          columns = argument.cols;
        } else if (columns != argument.cols) {
          throw new EvalException("number of columns of matrices must match");
        }
      } else {
        rows++;
      }
    }

    // if there are no actual matrices, then use the longest vector length as the number of columns
    if (columns == -1) {
      for (Combine.BindArgument argument : bindArguments) {
        if (argument.vector.length() > columns) {
          columns = argument.vector.length();
        }
      }
    }


    // now check that all vectors lengths are multiples of the column length
    for (Combine.BindArgument argument : bindArguments) {
      if (!argument.matrix) {
        if ((columns % argument.vector.length()) != 0) {
          throw new EvalException("number of columns of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    inspector.acceptAll(arguments);
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    Combine.Matrix2dBuilder builder = new Combine.Matrix2dBuilder(vectorBuilder, rows, columns);
    for (int j = 0; j != columns; ++j) {
      for (Combine.BindArgument argument : bindArguments) {
        for (int i = 0; i != argument.rows; ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = Null.INSTANCE;
    AtomicVector colNames = Null.INSTANCE;

    for (Combine.BindArgument argument : bindArguments) {
      if (argument.colNames.length() == columns) {
        colNames = argument.colNames;
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

    public void addFrom(Combine.BindArgument argument, int rowIndex, int colIndex) {
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

