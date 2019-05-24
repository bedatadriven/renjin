/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArguments;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.s4.S4;
import org.renjin.sexp.*;

import java.util.*;

/**
 * Primitives used in the implementation of the S3 object system
 */
public class S3 {


  public static final Set<String> GROUPS = Sets.newHashSet("Ops", "Math", "Summary");


  public static final Symbol METHODS_TABLE = Symbol.get(".__S3MethodsTable__.");

  public static StringVector computeObjectClass(Context context, SEXP object) {
    StringVector objectClasses = computeDataClasses(context, object);
    if(Types.isS4(object)) {
      SEXP objectClassesS4 = S4.computeDataClassesS4(context, objectClasses.getElementAsString(0));
      if (objectClassesS4 instanceof StringVector) {
        return (StringVector) objectClassesS4;
      }
    }
    return objectClasses;
  }


  @Internal
  public static SEXP NextMethod(@Current Context context,
                                @Current Environment rho,
                                SEXP genericArg,
                                SEXP objectArg,
                                @ArgumentList ListVector extraArgs) {

    // NextMethod is actually a closure defined as:
    //
    //   NextMethod <- function(generic=NULL, object=NULL, ...)
    //      .Internal(NextMethod(generic, object,...))
    //
    // And this closure must be called by the target method invoked by UseMethod or
    // NextMethod.
    //
    // So a call chain might look like:
    //
    // foo(x=1, 2)
    //  \
    //  UseMethod('foo')
    //  \
    //  foo.numeric(x=1,2) [previousContext]
    //   \
    //   NextMethod  [callingContext]
    //    \
    //    .Internal(NextMethod)
    //
    //
    // To continue dispatch, we need to retrieve the FunctionEnvironment associated with the previous
    // S3 method [previousContext].


    // But first, we need the callingContext, which may not be the same as the @Current context
    // if the call to NextMethod() has been passed to a closure and forced in a deeper context.

    Context callingContext = Contexts.findCallingContext(context, rho);

    // From the callingContext, we can obtain a reference to the DispatchTable which contains
    // the parameters used to dispatch to the previous method.

    FunctionEnvironment previousEnvironment;
    try {
      previousEnvironment = (FunctionEnvironment) callingContext.getCallingEnvironment();
    } catch (ClassCastException ignored) {
      throw new EvalException("'NextMethod' called from outside a function");
    }

    // ...and from the previousEnvironment we can find the previousContext...

    Context previousContext = callingContext;
    while(previousContext.getEnvironment() != previousEnvironment) {
      previousContext = previousContext.getParent();
    }

    DispatchTable dispatchTable = previousEnvironment.getDispatchTable();
    if(dispatchTable == null) {
      throw new EvalException("'NextMethod' must be called in a function invoked from UseMethod() or NextMethod()");
    }

    // The caller to NextMethod() can change the generic

    String generic = dispatchTable.getGeneric();
    if (genericArg != Null.INSTANCE) {
      generic = genericArg.asString();
    }

    DispatchTable nextTable = new DispatchTable(dispatchTable.getGenericDefinitionEnvironment(), generic);

    if (objectArg != Null.INSTANCE) {
      throw new UnsupportedOperationException("TODO: object arg to NextMethod");
    }

    List<String> nextClasses = nextClasses(dispatchTable);

    Function nextMethod = S3.findMethod(context,
        dispatchTable.getGenericDefinitionEnvironment(),
        previousEnvironment,
        dispatchTable.getGeneric(),
        dispatchTable.getGroup(),
        nextClasses,
        true,
        nextTable);

    // Update the arguments
    // If the previous method selected had the definition
    //
    //    function(x,y,z)
    //
    // And if it was called with the
    //
    //    foo.numeric(2, x=1, 3)
    //
    // And the variables are updated
    //
    //  x <- 91
    //  z <- 93
    //
    // Then we need to update the arguments with the new values
    // from the environment, without changing the order.
    //
    // For this reason, Closure.apply() stores the MatchedArguments
    // structure for use here.

    FunctionCall previousCall = previousContext.getCall();

    MatchedArguments previousArguments = dispatchTable.arguments;

    int numPrevious = previousArguments.getActualCount();

    // Number of extra arguments passed to NextMethod()
    int numExtra = extraArgs.length();

    // Allocate at most space for the original + extra arguments,
    // though the size may be smaller.

    int maxUpdatedArguments = numPrevious + numExtra;
    String[] updatedNames = Arrays.copyOf(previousArguments.getActualNames(), maxUpdatedArguments);
    SEXP[] updatedArguments = Arrays.copyOf(previousArguments.getActualValues(), maxUpdatedArguments);

    // Update the previous arguments with new values by name from the
    // environment. These will be default values or values explicitly replaced in the function body

    for (int formal = 0; formal < previousArguments.getFormalCount(); formal++) {
      int previousIndex = previousArguments.getActualIndex(formal);
      if(previousIndex != -1) {
        updatedArguments[previousIndex] = previousEnvironment.get(formal);
      }
    }

    if(numExtra != 0) {

      int actualIndex = numPrevious;
      for (int extraIndex = 0; extraIndex < numExtra; extraIndex++) {

        // Extra arguments to NextMethod() can either replace a previous argument,
        // if the names match exactly, or be added as a new argument.

        String extraName = Strings.emptyToNull(extraArgs.getName(extraIndex));
        int replacing = -1;
        if(extraName != null) {
          replacing = previousArguments.findActualIndexByName(extraName);
        }
        if(replacing != -1) {
          // Replace a previous argument's value
          updatedArguments[replacing] = extraArgs.getElementAsSEXP(extraIndex);

        } else {
          // Add a new argument to the end of the list
          updatedNames[actualIndex] = extraName;
          updatedArguments[actualIndex] = extraArgs.getElementAsSEXP(extraIndex);
          actualIndex++;
        }
      }

      // If we replaced any of the previous arguments with values from the
      // extraArgs list, our arrays will be too large. Resize to fit.

      if(actualIndex < maxUpdatedArguments) {
        updatedNames = Arrays.copyOf(updatedNames, actualIndex);
        updatedArguments = Arrays.copyOf(updatedArguments, actualIndex);
      }
    }


    // The new call that is visible to sys.call() and match.call()
    // is identical to the call which invoked UseMethod(), but we do update the function name.

    // For example, if you have a stack which looks like foo(x) -> UseMethod('foo') -> foo.default(x) then
    // the foo.default function will have a call of foo.default(x) visible to sys.call() and match.call()

    FunctionCall newCall = new FunctionCall(nextTable.getMethodSymbol(), previousCall.getArguments());

    return nextMethod.apply(context, rho, newCall, updatedNames, updatedArguments, nextTable);
  }


  /**
   *
   * @return remaining classes to be  tried after this method
   */
  private static List<String> nextClasses(DispatchTable table) {
    if(table.classVector == null) {
      return Collections.emptyList();
    }
    int classIndex = findIndex(table);
    List<String> next = new ArrayList<>();

    for (int i = classIndex + 1; i < table.classVector.length(); i++) {
      next.add(table.classVector.getElementAsString(i));
    }

    return next;
  }

  private static int findIndex(DispatchTable table) {
    for (int i = 0; i < table.classVector.length(); i++) {
      String className = table.classVector.getElementAsString(i);
      if(table.method.endsWith(className)) {
        return i;
      }
    }
    return table.classVector.length() - 1;
  }


  /**
   * Attempts to compute the classes used for S3 dispatch based on value bounds for an expression.
   * 
   * @param valueBounds
   * @return a StringVector containing the known class list for this expression, or {@code null} if they could not
   * be deduced.
   */
  public static StringVector computeDataClasses(ValueBounds valueBounds) {

    // If we don't know what the value's class attribute is, we can't make
    // any further assumptions
    if(valueBounds.hasUnknownClassAttribute()) {
      return null;
    }

    if(valueBounds.isFlagSet(ValueBounds.MAYBE_CLASS)) {
      AtomicVector classAttribute = valueBounds.getConstantClassAttribute();
      if (classAttribute.length() > 0) {
        // S3 class has been explicitly defined and is constant at compile time
        return (StringVector) classAttribute;
      }
    }
    
    // Otherwise we compute based on the type and dimensions
    // So in the absence of a constant class attribute, these two
    // properties need to be constant.
    if(!valueBounds.isDimCountConstant()) {
      return null;
    }

    int typeSet = valueBounds.getTypeSet();
    String implicitClass = TypeSet.implicitClass(typeSet);
    if(implicitClass == null) {
      return null;
    }
    
    StringArrayVector.Builder dataClass = new StringArrayVector.Builder();

    if(valueBounds.isFlagSet(ValueBounds.HAS_DIM2)) {
      dataClass.add("matrix");
    } else if(valueBounds.isFlagSet(ValueBounds.HAS_DIM)) {
      dataClass.add("array");
    }
    
    dataClass.add(implicitClass);
    
    if((typeSet & TypeSet.NUMERIC) != 0) {
      dataClass.add("numeric");
    }
    return dataClass.build();
  }
  

  /**
   * Computes the class list used for normal S3 Dispatch. Note that this
   * is different than the class() function
   */
  public static StringVector computeDataClasses(Context context, SEXP exp) {

    /*
     * Make sure we're dealing with the evaluated expression
     */
    exp = exp.force(context);

    SEXP classAttribute = exp.getAttribute(Symbols.CLASS);

    if(classAttribute.length() > 0) {
      /*
       * S3 Class has been explicitly defined
       */
      return (StringVector)classAttribute;
    } else {
      /*
       * Compute implicit class based on DIM attribute and type
       */
      StringArrayVector.Builder dataClass = new StringArrayVector.Builder();
      SEXP dim = exp.getAttribute(Symbols.DIM);
      if(dim.length() == 2) {
        dataClass.add("matrix");
      } else if(dim.length() > 0) {
        dataClass.add("array");
      }
      if(exp instanceof IntVector) {
        dataClass.add("integer");
        dataClass.add("numeric");
      } else if(exp instanceof DoubleVector) {
        dataClass.add("double");
        dataClass.add("numeric");
      } else {
        dataClass.add(exp.getImplicitClass());
      }
      return dataClass.build();
    }
  }

  public static SEXP tryDispatchFromPrimitive(Context context,
                                       Environment rho,
                                       FunctionCall call,
                                       String generic,
                                       String group,
                                       String[] argumentNames,
                                       SEXP[] arguments) {

    /*
     * First we need to see if we've already been through this.
     * (It's complicated)
     *
     * - This method is called by "generic" primitives that, before doing
     *   their normal thing, check to see if there's a user-defined function
     *   out there that provides specific behavior for the object.
     *
     * - It can happen that that user function in turn calls NextMethod()
     *   to defer to the default behavior of the primitive. In this case,
     *   we don't want to check again because we've already been through
     *   that; to do so would be to loop infinitely.
     *
     * - We can only tell whether this is the first or second time around,
     *   because if it's the second, NextMethod() will invoke the primitive
     *   by the name "<primitive>.default", like "as.character.default".
     */
    if(call.getFunction() instanceof Symbol &&
            ((Symbol) call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }

    boolean isOps = "Ops".equals(group);

    int nargs;
    if (isOps) {
      nargs = argumentNames.length;
    } else {
      nargs = 1;
    }

    for (int k = 0; k < nargs; k++) {
      SEXP argument = arguments[k].force(context);
      if(Types.isS4(argument)) {
        throw new UnsupportedOperationException("TODO");
        //return S4.tryS4DispatchFromPrimitive(context, argument, args, rho, group, generic);
      }
    }

    if(generic.equals("%*%")) {
      return null;
    }

    Environment definitionEnvironment = context.getBaseEnvironment();


    SEXP left = arguments[0].force(context);
    StringVector leftClasses = computeDataClasses(context, left);

    DispatchTable dispatchTable = new DispatchTable(definitionEnvironment, generic, leftClasses);

    Function leftMethod = findMethod(context, definitionEnvironment, rho, generic, group, leftClasses, false, dispatchTable);

    /*
     * For binary generics in the Ops group, we need to do additional work.
     *
     * For these generics (and only these generics), we attempt a match on both the left
     * AND right arguments. If only one matches a method, that match is used. But if they
     * match TWO DIFFERENT methods, then a warning is emitted and we fallback to the default implementation.
     *
     * The .Method value in the environment of the dispatched function is then
     * also exceptionally a vector of length two, and shows which argument was used for dispatch.
     *
     * For example:
     *
     * .Method == c("Ops.foo", "")        => dispatched to Ops.foo on only the first argument; second argument was
     *                                       not an object, or was of a class with no Ops method defined.
     * .Method == c("", "Ops.foo")        => dispatched only on the second argument
     * .Method == c("Ops.foo", "Ops.foo") => dispatched on both arguments; both had a class of "foo"
     *
     * The second element of the .Method vector is stored in the method2 field of DispatchTable. That way we
     * only have to allocate a StringArrayVector if really used.
     */
    if(nargs == 2) {


      // Verify that the second argument is either not an object, or at least results in selecting the
      // same method

      dispatchTable.method2 = "";

      SEXP right = arguments[1].force(context);
      if(right.isObject()) {
        StringVector rightClasses = computeDataClasses(context, right);
        Function rightMethod = findMethod(context, definitionEnvironment, rho, generic, group, rightClasses, false, dispatchTable);

        if(leftMethod != null && rightMethod != null && rightMethod != leftMethod) {
          context.warn("Incompatible methods for \"" + generic + "\"");
          return null;

        } else if(leftMethod == null) {

          // Dispatching on the class of the right argument.

          leftMethod = rightMethod;
          dispatchTable.classVector = rightClasses;

          // findMethod() sets the "method" field. Swap the value to method2 and
          // set "method" to blank.
          dispatchTable.method2 = dispatchTable.method;
          dispatchTable.method = "";

        } else {
          // Dispatching on both the left and right argument because
          // they have the same affect.
          dispatchTable.method2 = dispatchTable.method;
        }
      }
    }

    if(leftMethod == null) {
      return null;
    }


    if(isOps) {

      /*
       * Ops functions drop their names
       */
      Arrays.fill(argumentNames, null);


    } else if("Summary".equals(group)) {

      // When dispatching to S3 summary methods, we pretend that the summary
      // builtin has an extra na.rm argument with default value false.
      boolean naRmSupplied = false;
      int numArgs = argumentNames.length;
      for (int i = 0; i < numArgs; i++) {
        if("na.rm".equals(argumentNames[i])) {
          naRmSupplied = true;
        }
      }
      if(!naRmSupplied) {
        argumentNames = Arrays.copyOf(argumentNames, numArgs + 1);
        arguments = Arrays.copyOf(arguments, numArgs + 1);
        argumentNames[numArgs] = "na.rm";
        arguments[numArgs] = LogicalVector.FALSE;
      }
    }

    return leftMethod.apply(context, rho, call, argumentNames, arguments, dispatchTable);
  }


  public static Environment findMethodTable(Context context, Environment definitionEnvironment) {
    SEXP table = definitionEnvironment.getVariableUnsafe(METHODS_TABLE).force(context);
    if(table instanceof Environment) {
      return (Environment) table;
    } else if(table == Symbol.UNBOUND_VALUE) {
      return Environment.EMPTY;
    } else {
      throw new EvalException("Unexpected value for .__S3MethodsTable__. in " + definitionEnvironment.getName());
    }
  }

  public static Function findMethod(Context context,
                                    Environment definitionEnvironment,
                                    Environment callingEnvironment,
                                    String genericMethodName,
                                    String group,
                                    Iterable<String> classes,
                                    boolean searchForDefault,
                                    DispatchTable dispatchTable) {

    Environment methodTable = findMethodTable(context, definitionEnvironment);
    Function method;

    for(String className : classes) {

      method = findMethod(context, methodTable, callingEnvironment, genericMethodName, className, dispatchTable);
      if(method != null) {
        return method;
      }
      if(group != null) {
        method = findMethod(context, methodTable, callingEnvironment, group, className, dispatchTable);
        if(method != null) {
          return method;
        }
      }
    }

    if(!searchForDefault) {
      return null;
    }

    //---this is from nextOrDefault() //

    // Look up the .default method first in the definition environment
    Function function = findMethod(context, methodTable, definitionEnvironment, genericMethodName, "default", dispatchTable);
    if(function != null) {
      dispatchTable.classVector = null;
      return function;
    }

    // Otherwise see if *another* package has defined a default method
    function = findMethod(context, methodTable, callingEnvironment, genericMethodName, "default", dispatchTable);
    if(function != null) {
      dispatchTable.classVector = null;
      return function;
    }

    // as a last step, we call BACK into the primitive
    // to get the default implementation  - ~ YECK ~
    PrimitiveFunction primitive = Primitives.getBuiltin(genericMethodName);
    if(primitive != null) {
      dispatchTable.method = genericMethodName + ".default";
      return primitive;
    }

    return null;
  }

  private static Function findMethod(Context context,
                                     Environment methodTable,
                                     Environment callingEnvironment,
                                     String name,
                                     String className,
                                     DispatchTable dispatchTable) {

    String method = name + "." + className;
    Symbol methodSymbol = Symbol.get(method);
    Function function = callingEnvironment.findFunction(context, methodSymbol);
    if(function != null) {
      dispatchTable.method = methodSymbol.getPrintName();
      return function;

    } else if(methodTable.hasVariable(methodSymbol)) {
      dispatchTable.method = methodSymbol.getPrintName();
      return (Function) methodTable.getVariableUnsafe(methodSymbol).force(context);

    } else {
      return null;
    }
  }

}
