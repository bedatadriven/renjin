package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

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

    // establish the number of rows
    // 1. check actual matrices
    int rows = -1;
    int nonNullCount = 0;
    for (BindArgument argument : bindArguments) {
      if (argument.getVector() != Null.INSTANCE) {
        nonNullCount++;
      }
      if (argument.isMatrix()) {
        if (rows == -1) {
          rows = argument.getRows();
        } else if (rows != argument.getRows()) {
          throw new EvalException("number of rows of matrices must match");
        }
      }
    }
    
    if(nonNullCount == 0) {
      return Null.INSTANCE;
    }

    // 2. if there are no actual matrices, then use the longest vector length as the number of rows
    if (rows == -1) {
      for (BindArgument argument : bindArguments) {
        if (argument.getVector().length() > rows) {
          rows = argument.getVector().length();
        }
      }
    }

    // now check that all vectors are multiples of the row length
    for (BindArgument argument : bindArguments) {
      if (!argument.isMatrix()) {
        if (!argument.isZeroLength() && (rows % argument.getVector().length()) != 0) {
          throw new EvalException("number of rows of result is not a multiple of vector length");
        }
      }
    }

    // get the common type and a new builder
    Inspector inspector = new Inspector(false);
    for (BindArgument bindArgument : bindArguments) {
      if(bindArgument.getVector() != Null.INSTANCE) {
        bindArgument.getVector().accept(inspector);
      }
    }
    Vector.Builder vectorBuilder = inspector.getResult().newBuilder();

    // now calculate the number of columns and 
    // determine which arguments we're actually keeping
    int columns = 0;
    List<BindArgument> retained = new ArrayList<>(bindArguments.size());
    for (BindArgument bindArgument : bindArguments) {
      if (bindArgument.isMatrix()) {
        columns += bindArgument.getCols();
        retained.add(bindArgument);
        
      } else if (bindArgument.isZeroLength()) {
        // Only count zero-length vectors if 
        // nrows == 0
        if(rows == 0) {
          columns++;
          retained.add(bindArgument);
        } 
      } else {
        // Non-zero length vectors are always included
        columns++;
        retained.add(bindArgument);
      }
    }
    
    // wrap the builder
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for (BindArgument argument : retained) {
      if(!argument.isZeroLength()) {
        for (int j = 0; j != argument.getCols(); ++j) {
          for (int i = 0; i != rows; ++i) {
            builder.addFrom(argument, i, j);
          }
        }
      }
    }

    StringVector.Builder rowNames = new StringVector.Builder();
    StringVector.Builder colNames = new StringVector.Builder();

    boolean hasRowNames = false;
    boolean hasColNames = false;

    for (BindArgument argument : retained) {
      if (argument.getRowNames() != Null.INSTANCE) {
        hasRowNames = true;
        if (rowNames.length() < argument.getRowNames().length()) {
          for (int i = 0; i != argument.getRows(); ++i) {
            rowNames.add(argument.getRowNames().getElementAsString(i));
          }
        }
      } else if(!argument.isZeroLengthVector() || rows == 0) {
        if (rowNames.length() < argument.getRowNames().length()) {
          for (int i = 0; i != argument.getRows(); ++i) {
            rowNames.add("");
          }
        }
      }
    }

    for (BindArgument argument : retained) {
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
    
    if(allZeroLengthVectors(retained)) {
      // This doesn't seem like a great choice, but reproduce
      // behavior of GNU R
      builder.setDimNames(Null.INSTANCE, Null.INSTANCE);
    
    } else if(hasColNames || hasRowNames) {

      builder.setDimNames(
          hasRowNames ? rowNames.build() : Null.INSTANCE,
          hasColNames ? colNames.build() : Null.INSTANCE);
    }
     
    return builder.build();
  }
  
  private static boolean allZeroLengthVectors(List<BindArgument> bindArguments) {
    for (BindArgument bindArgument : bindArguments) {
      if(!bindArgument.isZeroLengthVector()) {
        return false;
      }
    }
    return true;
  }
  
}