package org.renjin.primitives.combine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.plexus.util.cli.Commandline;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.sexp.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.renjin.primitives.Deparse;

import static org.renjin.primitives.Deparse.deparse;

/**
 * Special function to do cbind and use objectnames as columnnames
 */
public class ColumnBindFunction extends AbstractBindFunction {

  public ColumnBindFunction() {
    super("cbind");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList arguments) {

    ArgumentIterator argumentItr = new ArgumentIterator(context, rho, arguments);
    int deparseLevel = ((Vector) argumentItr.evalNext()).getElementAsInt(0);
    List<BindArgument> bindArguments = createBindArgument(context, rho, deparseLevel, false, argumentItr);

    SEXP genericResult = tryBindDispatch(context, rho, "cbind", deparseLevel, bindArguments);
    if (genericResult != null) {
      return genericResult;
    }

    List<BindArgument> cleanBindArguments = cleanBindArguments(bindArguments);

    // establish the number of rows
    // 1. check actual matrices
    int rows = -1;
    int columns = 0;
    for (BindArgument argument : cleanBindArguments) {
      if (argument.getVector().length() > 0) {
        if (argument.isMatrix()) {
          columns += argument.getCols();
          if (rows == -1) {
            rows = argument.getRows();
          } else if (rows != argument.getRows()) {
            throw new EvalException("number of rows of matrices must match");
          }
        } else {
          columns++;
        }
      }
    }

    if (columns == 0) {
      return Null.INSTANCE;
    }

    // if there are no actual matrices, then use the longest vector length as the number of rows
    if (rows == -1) {
      for (BindArgument argument : cleanBindArguments) {
        if (argument.getVector().length() > rows) {
          rows = argument.getVector().length();
        }
      }
    }

    // now check that all vectors lengths are multiples of the column length
    for (BindArgument argument : cleanBindArguments) {
      if (!argument.isMatrix()) {
        if ((rows % argument.getVector().length()) != 0) {
          throw new EvalException("number of rows of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    for (BindArgument bindArgument : cleanBindArguments) {
      bindArgument.getVector().accept(inspector);
    }
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for (BindArgument argument : cleanBindArguments) {
      for (int j = 0; j != argument.getCols(); ++j) {
        for (int i = 0; i != rows; ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    StringVector.Builder rowNames = new StringVector.Builder();
    StringVector.Builder colNames = new StringVector.Builder();

    boolean hasRowNames = false;
    boolean hasColNames = false;

    for (BindArgument argument : cleanBindArguments) {
      if (argument.getRowNames() != Null.INSTANCE) {
        hasRowNames = true;
        if (rowNames.length() < argument.getRowNames().length()) {
          for (int i = 0; i != argument.getRows(); ++i) {
            rowNames.add(argument.getRowNames().getElementAsString(i));
          }
        }
      } else {
        if (rowNames.length() < argument.getRowNames().length()) {
          for (int i = 0; i != argument.getRows(); ++i) {
            rowNames.add("");
          }
        }
      }
    }

    for (BindArgument argument : cleanBindArguments) {
      if (argument.getColNames() != Null.INSTANCE) {
        hasColNames = true;
        for (int i = 0; i != argument.getCols(); ++i) {
          colNames.add(argument.getColNames().getElementAsString(i));
        }
      } else if (!argument.hasNoName() && !argument.isMatrix()) {
        colNames.add(argument.getName());
        hasColNames = true;
      } else {
        for (int i = 0; i != argument.getCols(); ++i) {
          colNames.add("");
        }
      }
    }

    if (deparseLevel == 0) {
      hasColNames = false;
    }

    builder.setDimNames(hasRowNames ? rowNames.build() : Null.INSTANCE, hasColNames ? colNames.build() : Null.INSTANCE);

    return builder.build();
  }
}