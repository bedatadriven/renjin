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

    List<BindArgument> bindArguments = Lists.newArrayList();

    while(argumentItr.hasNext()) {
      PairList.Node currentNode = argumentItr.nextNode();
      SEXP evaluated = context.evaluate(currentNode.getValue(), rho);
      bindArguments.add(new BindArgument(currentNode.getName(),(Vector) evaluated, true, currentNode.getValue()));
    }

    SEXP genericResult = tryBindDispatch(context, rho, "rbind", deparseLevel, bindArguments);
    if (genericResult != null) {
      return genericResult;
    }

    for (int i = 0; i < bindArguments.size(); i++) {
      if (bindArguments.get(i).vector.length() == 0) {
        bindArguments.remove(i);
      }
    }

    // establish the number of columns
    // 1. check actual matrices
    int rows = 0;
    int columns = -1;
    for (BindArgument argument : bindArguments) {
      if (argument.vector.length() > 0) {

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
    }

    if (rows == 0) {
      return Null.INSTANCE;
    }

    // if there are no actual matrices, then use the longest vector length as the number of columns
    if (columns == -1) {
      for (BindArgument argument : bindArguments) {
        if (argument.vector.length() > columns) {
          columns = argument.vector.length();
        }
      }
    }


    // now check that all vectors lengths are multiples of the column length
    for (BindArgument argument : bindArguments) {
      if (!argument.matrix) {
        if ( argument.vector.length() > 0 && (columns % argument.vector.length()) != 0) {
          throw new EvalException("number of columns of result is not a multiple of vector length");
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
    for (int j = 0; j != columns; ++j) {
      for (BindArgument argument : bindArguments) {
        for (int i = 0; i != argument.rows; ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = Null.INSTANCE;
    AtomicVector colNames = Null.INSTANCE;

    for (BindArgument argument : bindArguments) {
      if (argument.colNames.length() == columns) {
        colNames = argument.colNames;
        break;
      }
    }

    builder.setDimNames(rowNames, colNames);

    return builder.build();
  }
}

