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

    int deparseLevel = ((Vector) context.evaluate( call.getArgument(0), rho)).getElementAsInt(0);

    SEXP genericResult = tryBindDispatch(context, rho, "rbind", deparseLevel, arguments);
    if (genericResult != null) {
      return genericResult;
    }

    List<BindArgument> bindArguments = Lists.newArrayList();
    Map<Symbol, SEXP> propertyValues = Maps.newHashMap();

    ArgumentIterator argumentItr = new ArgumentIterator(context, rho, arguments);
    while(argumentItr.hasNext()) {
      PairList.Node currentNode = argumentItr.nextNode();
      SEXP evaled = context.evaluate( currentNode.getValue(), rho);

      if(currentNode.hasTag()) {
        propertyValues.put(currentNode.getTag(), evaled);
      } else {
        bindArguments.add(new BindArgument(currentNode.getName(), (Vector) evaled, false));
      }
    }

    bindArguments.remove(0);

    if (bindArguments.isEmpty()) {
      return Null.INSTANCE;
    }

    // establish the number of columns
    // 1. check actual matrices
    int columns = -1;
    int rows = 0;
    for (BindArgument argument : bindArguments) {
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
    inspector.acceptAll(arguments.values());
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

