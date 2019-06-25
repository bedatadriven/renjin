/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.special;

import org.renjin.eval.*;
import org.renjin.invoke.codegen.WrapperRuntime;
import org.renjin.primitives.Contexts;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;

public class UseMethod extends SpecialFunction {

  public UseMethod() {
    super("UseMethod");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {

    int numUseMethodArgs = call.getArguments().length();
    if (numUseMethodArgs == 0) {
      throw new EvalException("there must be a 'generic' argument");
    }

    SEXP genericSexp = context.evaluate(call.getArgument(0), rho);
    if (!(genericSexp instanceof StringVector)) {
      throw new EvalException("'generic' must be a character string");
    }

    String generic = WrapperRuntime.convertToString(genericSexp);


    /*
     * The functionEnvironment is the environment of the function which calls UseMethod()
     *
     * So in the example:
     *
     * foo     <- function(a) as.list(a)
     * as.list <- function(x,...) UseMethod("as.list")    <---- functionEnvironment
     *
     * The functionEnvironment contains only two symbols:
     *
     * x   :   the first argument
     * ... :   the PromisePairList
     *
     */
    FunctionEnvironment functionEnvironment = (FunctionEnvironment) rho;

    /*
     * Part I: Prepare the new argument list
     *
     */

    /*
     * UseMethod() is very special.
     *
     * It selects and invokes a method based on the class of an argument, but it is not
     * passed the arguments directly.
     *
     * Rather, it inherits its arguments from the *calling function*.
     *
     * For example:
     *
     * foo     <- function(a) as.list(a)
     * as.list <- function(x,...) UseMethod("as.list")
     *
     * The *calling arguments* are the arguments to the as.list() function, which include
     * "a" above. This
     *
     * The first argument has to be handled specially to ensure that it is not re-evaluated.
     *
     */

    MatchedArguments previousArguments = functionEnvironment.getMatching();

    Context callingContext = Contexts.findCallingContext(context, functionEnvironment);

    SEXP object = findObject(context, rho, call.getArguments(), functionEnvironment);

    /*
     * From the "object", we compute a list of classes that are used to select a method.
     */
    StringVector classes = S3.computeDataClasses(object);


    /*
     * Part II: Select the method to invoke
     */


    /*
     * The definition environment is the environment in which the original generic function was defined.
     *
     * So if you have generic function "foo":
     *
     * f <- function(x) UseMethod('foo')
     *
     * Then the definition environment is the one in which the statement above was evaluated.
     */
    Environment definitionEnvironment = ((Closure) context.getFunction()).getEnclosingEnvironment();

    /*
     * The dispatch table is an array of values that are visible to the method that we are about to call.
     * It contains the following symbols:
     * .Generic
     * .Method
     * .Classes
     * etc
     *
     * We store it as an array.
     */
    S3DispatchMetadata dispatchTable = new S3DispatchMetadata(definitionEnvironment, generic, classes);

    /*
     * Find the method!
     */
    Function method = S3.findMethod(context, definitionEnvironment, rho, generic, null, classes, true, dispatchTable);
    if (method == null) {
      throw new EvalException("no applicable method for '%s' applied to an object of class \"%s\"",
          generic, classes.toString());
    }

    /*
     * Callers to UseMethod() may also provide additional arguments to be passed to the selected method.
     * But this is rarely used.
     */
    if (numUseMethodArgs > 2) {
      throw new EvalException("TODO: Extra arguments to UseMethod()");
    }


    /*
     * The new call that is visible to sys.call() and match.call()
     * is identical to the call which invoked UseMethod(), but we do update the function name.

     * For example, if you have a stack which looks like foo(x) -> UseMethod('foo') -> foo.default(x) then
     * the foo.default function will have a call of foo.default(x) visible to sys.call() and match.call()
     */
    FunctionCall newCall = new FunctionCall(dispatchTable.getMethodSymbol(), callingContext.getCall().getArguments());


    /*
     * Finally invoke the selected methods with the re-promised arguments
     */
    return method.applyPromised(
        context,
        callingContext.getCallingEnvironment(),
        new ArgList(previousArguments.getActualNames(),
            previousArguments.getActualValues()),
        newCall,
        dispatchTable);
  }


  /*
   * Find the "object" on which to dispatch.
   */
  private SEXP findObject(Context context, Environment callingEnvironment, PairList useMethodArgs, FunctionEnvironment functionEnvironment) {

    /*
     * If a second argument to UseMethod is provided, then the value of that argument is used
     * to dispatch the call. That is to say, that class of that argument's value determines which
     * method is selected.
     *
     * It *does not* however, replace the first callingArgument when the selected method is
     * invoked
     */
    if (useMethodArgs.length() > 1) {
      return context.evaluate(useMethodArgs.getElementAsSEXP(1), callingEnvironment);
    }

    /*
     * If not, we must dispatch on the first *matched* argument.
     */
    if(functionEnvironment.getFormalCount() > 0) {

      SEXP firstArgument = functionEnvironment.getFormalValue(0);
      if (firstArgument != null) {

        /*
         * It can be that the first formal is the ellipses (...).
         * In the case, we dispatch on the first argument
         */

        if (functionEnvironment.getLocalName(0) == Symbols.ELLIPSES) {
          if (firstArgument instanceof PromisePairList.Node) {
            return ((PromisePairList.Node) firstArgument).getValue().force(context);
          } else {
            return Null.INSTANCE;
          }

        } else {

          /*
           * Otherwise, it is the argument matched to the first formal.
           */
          return firstArgument.force(context);
        }
      }

    }
    return Null.INSTANCE;
  }
}
