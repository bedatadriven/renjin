package org.renjin.primitives.combine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.sexp.*;

import java.util.List;
import java.util.Map;

/**
 * Special function to do rbind and use objectnames as rownames
 */
public class RowBindFunction extends AbstractBindFunction {

  public RowBindFunction() {
    super("rbind");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList arguments) {

    ArgumentIterator argumentItr = new ArgumentIterator(context, rho, arguments);
    int deparseLevel = ((Vector) argumentItr.evalNext()).getElementAsInt(0);
    List<BindArgument> bindArguments = createBindArgument(context, rho, deparseLevel, true, argumentItr);

    SEXP genericResult = tryBindDispatch(context, rho, "rbind", deparseLevel, bindArguments);
    if (genericResult != null) {
      return genericResult;
    }

    List<BindArgument> cleanBindArguments = cleanBindArguments(bindArguments);

    // establish the number of columns
    // 1. check actual matrices
    int rows = 0;
    int columns = -1;
    for (BindArgument argument : cleanBindArguments) {
      if (argument.getVector().length() > 0) {
        if (argument.isMatrix()) {
          rows += argument.getRows();
          if (columns == -1) {
            columns = argument.getCols();
          } else if (columns != argument.getCols()) {
            throw new EvalException("number of columns of matrices must match");
          }
        } else {
          rows++;
        }
      }
    }

    if (rows == 0) {
      return Null.INSTANCE;
    }

    // if there are no actual matrices, then use the longest vector length as the number of columns
    if (columns == -1) {
      for (BindArgument argument : cleanBindArguments) {
        if (argument.getVector().length() > columns) {
          columns = argument.getVector().length();
        }
      }
    }

    // now check that all vectors lengths are multiples of the column length
    for (BindArgument argument : cleanBindArguments) {
      if (!argument.isMatrix()) {
        if ((columns % argument.getVector().length()) != 0) {
          throw new EvalException("number of columns of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    for (BindArgument argument : cleanBindArguments) {
      argument.getVector().accept(inspector);
    }
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for (int j = 0; j != columns; ++j) {
      for (BindArgument argument : cleanBindArguments) {
        for (int i = 0; i != argument.getRows(); ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    StringVector.Builder colNames = new StringVector.Builder();
    StringVector.Builder rowNames = new StringVector.Builder();

    boolean hasColNames = false;
    boolean hasRowNames = false;

    for (BindArgument argument : cleanBindArguments) {
      if (argument.getColNames() != Null.INSTANCE) {
        hasColNames = true;
        if (colNames.length() < argument.getColNames().length()) {
          for (int i = 0; i != argument.getCols(); ++i) {
            colNames.add(argument.getColNames().getElementAsString(i));
          }
        }
      } else {
        if (colNames.length() < argument.getColNames().length()) {
          for (int i = 0; i != argument.getCols(); ++i) {
            colNames.add("");
          }
        }
      }
    }

    for (BindArgument argument : cleanBindArguments) {
      if (argument.getRowNames() != Null.INSTANCE) {
        hasRowNames = true;
        for (int i = 0; i != argument.getRows(); ++i) {
          rowNames.add(argument.getRowNames().getElementAsString(i));
        }
      } else if (!argument.hasNoName() && !argument.isMatrix()) {
        rowNames.add(argument.getName());
        hasRowNames = true;
      } else {
        for (int i = 0; i != argument.getRows(); ++i) {
          rowNames.add("");
        }
      }
    }

    if (deparseLevel == 0) {
      hasRowNames = false;
    }

    builder.setDimNames(hasRowNames ? rowNames.build() : Null.INSTANCE, hasColNames ? colNames.build() : Null.INSTANCE);

    return builder.build();
  }
}

