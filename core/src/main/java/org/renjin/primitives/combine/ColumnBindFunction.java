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

    List<BindArgument> bindArguments = Lists.newArrayList();

    while(argumentItr.hasNext()) {
      PairList.Node currentNode = argumentItr.nextNode();
      SEXP evaluated = context.evaluate(currentNode.getValue(), rho);
      bindArguments.add(new BindArgument(currentNode.getName(),(Vector) evaluated, false));
    }

    SEXP genericResult = tryBindDispatch(context, rho, "cbind", deparseLevel, bindArguments);
    if (genericResult != null) {
      return genericResult;
    }

    for (int i = 0; i < bindArguments.size(); i++) {
      if (bindArguments.get(i).vector.length() == 0) {
        bindArguments.remove(i);
      }
    }

    // establish the number of rows
    // 1. check actual matrices
    int rows = -1;
    int columns = 0;
    for (BindArgument argument : bindArguments) {
      if (argument.vector.length() > 0) {
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

    }

    if (columns == 0) {
      return Null.INSTANCE;
    }

    // if there are no actual matrices, then use the longest vector length as the number of rows
    if (rows == -1) {
      for (BindArgument argument : bindArguments) {
        if (argument.vector.length() > rows) {
          rows = argument.vector.length();
        }
      }
    }

    // now check that all vectors lengths are multiples of the column length
    for (BindArgument argument : bindArguments) {
      if (!argument.matrix) {
        if (argument.vector.length() > 0 && (rows % argument.vector.length()) != 0) {
          throw new EvalException("number of rows of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    for (BindArgument bindArgument : bindArguments) {
      bindArgument.vector.accept(inspector);
    }
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // wrap the builder
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for (BindArgument argument : bindArguments) {
      for (int j = 0; j != argument.cols; ++j) {
        for (int i = 0; i != rows; ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = Null.INSTANCE;
    StringVector.Builder colNames = new StringVector.Builder();

    boolean hasColNames = false;

    for (BindArgument argument : bindArguments) {
      if (argument.rowNames.length() == rows) {
        rowNames = argument.rowNames;
        break;
      }
    }
    for (BindArgument argument : bindArguments) {
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
}