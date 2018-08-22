/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.primitives.S3;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

/**
 * methods used by both cbind and rbind
 */
public abstract class AbstractBindFunction extends SpecialFunction {

  private MatrixDim bindDim;

  protected AbstractBindFunction(String name, MatrixDim bindDim) {
    super(name);
    this.bindDim = bindDim;
  }

  @Override
  public final SEXP apply(Context context, Environment rho, FunctionCall call, PairList arguments) {

    ArgumentIterator argumentItr = new ArgumentIterator(context, rho, arguments);
    int deparseLevel = ((Vector) argumentItr.evalNext()).getElementAsInt(0);

    List<BindArgument> bindArguments = Lists.newArrayList();
    while(argumentItr.hasNext()) {
      PairList.Node currentNode = argumentItr.nextNode();
      SEXP evaluated = context.evaluate(currentNode.getValue(), rho);
      bindArguments.add(new BindArgument(currentNode.getName(), (Vector) evaluated, bindDim, 
          currentNode.getValue(), deparseLevel, context));
    }
    
    SEXP genericResult = tryBindDispatch(context, rho, getName(), deparseLevel, bindArguments);
    if (genericResult != null) {
      return genericResult;
    }

    if(onlyNullArguments(bindArguments)) {
      return Null.INSTANCE;
    }
    return apply(context, bindArguments);

  }

  protected abstract SEXP apply(Context context, List<BindArgument> bindArguments);


  protected static boolean allZeroLengthVectors(List<BindArgument> bindArguments) {
    for (BindArgument bindArgument : bindArguments) {
      if(!bindArgument.isZeroLengthVector()) {
        return false;
      }
    }
    return true;
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
   */
  public static SEXP tryBindDispatch(Context context, Environment rho,
                                     String bindFunctionName, int deparseLevel, List<BindArgument> arguments) {

    Symbol foundMethod = null;
    org.renjin.sexp.Function foundFunction = null;

    // Get the base method table
    Environment methodsTable = (Environment) context
        .getBaseEnvironment()
        .getVariableUnsafe(S3.METHODS_TABLE)
        .force(context);

    for(BindArgument argument : arguments) {
      Vector classes = argument.getClasses();
      for(int i=0;i!=classes.length();++i) {
        Symbol methodName = Symbol.get(bindFunctionName + "." + classes.getElementAsString(i));
        org.renjin.sexp.Function function = rho.findFunction(context, methodName);
        if(function == null) {
          function = methodsTable.findFunction(context, methodName);
        }
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

    for (BindArgument argument : arguments) {
      args.add(argument.getArgName(), argument.repromise());
    }

    PairList buildArgs = args.build();

    FunctionCall call = new FunctionCall(Symbol.get(bindFunctionName), buildArgs);
    return foundFunction.apply(context, rho, call, buildArgs);
  }

  /**
   * Returns true if all arguments are NULL
   */
  private boolean onlyNullArguments(List<BindArgument> bindArguments) {
    for (BindArgument bindArgument : bindArguments) {
      if(!bindArgument.isNull()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Finds the common number of rows or column of the matrix arguments, throwing
   * an error if they do not match.
   * @return the common number of rows or columns, or -1 if there are no matrices.
   */
  protected int findCommonMatrixDimLength(List<BindArgument> bindArguments, MatrixDim dim) {
    int length = -1;
    for (BindArgument argument : bindArguments) {
      if (argument.isMatrix()) {
        if (length == -1) {
          length = argument.getDimLength(dim);
        } else if (length != argument.getDimLength(dim)) {
          throw new EvalException("number of %s of matrices must match", dim.name().toLowerCase() + "s");
        }
      }
    }
    return length;
  }

  /**
   * Finds the maximum vector length of the arguments
   */
  protected static int findMaxLength(List<BindArgument> bindArguments) {
    int length = 0;
    for (BindArgument argument : bindArguments) {
      if (argument.getVector().length() > length) {
        length = argument.getVector().length();
      }
    }
    return length;
  }

  protected void warnIfVectorLengthsAreNotMultiple(Context context, List<BindArgument> bindArguments, int expectedLength) {
    for (BindArgument argument : bindArguments) {
      if (!argument.isMatrix()) {
        if (!argument.isZeroLength() && (expectedLength % argument.getVector().length()) != 0) {
          context.warn("number of " + (bindDim == MatrixDim.COL ? "rows" : "cols") + 
              " of result is not a multiple of vector length");
        }
      }
    }
  }

  protected Vector.Builder builderForCommonType(List<BindArgument> bindArguments) {
    Inspector inspector = new Inspector(false);
    for (BindArgument bindArgument : bindArguments) {
      if(bindArgument.getVector() != Null.INSTANCE) {
        bindArgument.getVector().accept(inspector);
      }
    }
    return inspector.getResult().newBuilder();
  }

  protected List<BindArgument> excludeZeroLengthVectors(List<BindArgument> bindArguments) {
    List<BindArgument> retained = new ArrayList<>(bindArguments.size());
    for (BindArgument bindArgument : bindArguments) {
      if(!bindArgument.isZeroLengthVector()) {
        retained.add(bindArgument);
      }
    }
    return retained;
  }

  protected int countRowOrCols(List<BindArgument> bindArguments, MatrixDim dim) {
    int count = 0;
    for (BindArgument bindArgument : bindArguments) {
      count += bindArgument.getDimLength(dim);
    }
    return count;
  }

  protected AtomicVector combineDimNames(List<BindArgument> bindArguments, MatrixDim dim) {
    boolean hasNames = false;
    StringVector.Builder resultNames = new StringVector.Builder();

    for (BindArgument argument : bindArguments) {
      AtomicVector argumentNames = argument.getNames(dim);
      if (argumentNames != Null.INSTANCE) {
        hasNames = true;
        for (int i = 0; i != argumentNames.length(); ++i) {
          resultNames.add(argumentNames.getElementAsString(i));
        }

      } else if (!argument.hasNoName() && !argument.isMatrix()) {
        resultNames.add(argument.getName());
        hasNames = true;

      } else {
        for (int i = 0; i != argument.getDimLength(dim); ++i) {
          resultNames.add("");
        }
      }
    }  
    if(hasNames) {
      return resultNames.build();
    } else {
      return Null.INSTANCE;
    }
  }

  protected AtomicVector dimNamesFromLongest(List<BindArgument> bindArguments, MatrixDim dim, int dimLength) {
    for (BindArgument argument : bindArguments) {
      if (argument.getDimLength(dim) == dimLength) {
        AtomicVector argumentNames = argument.getNames(dim);
        if (argumentNames != Null.INSTANCE) {
          return argumentNames;
        }
      }
    }
    return Null.INSTANCE;
  }
}
