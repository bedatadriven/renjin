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
package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.methods.MethodDispatch;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Implementations of the subset operators {@code [}, {@code [[}, {@code $}, and {@code @}
 * and their sub-assignment counterparts.
 */
public class Subsetting {

  private Subsetting() {

  }

  private static Symbol asSymbol(SEXP nameExp) {
    if(nameExp instanceof Symbol) {
      return (Symbol) nameExp;
    } else if(nameExp instanceof StringVector && nameExp.length() == 1) {
      return Symbol.get(((StringVector) nameExp).getElementAsString(0));
    } else {
      throw new EvalException("illegal argument: " + nameExp);
    }
  }

  public static SEXP setElementByName(ExternalPtr<?> externalPtr, String name, SEXP value) {
    externalPtr.setMember(Symbol.get(name), value);
    return externalPtr;
  }

  @Builtin("@")
  public static SEXP getSlotValue(@Current Context context, @Current MethodDispatch methods, SEXP object,
                                  @Unevaluated Symbol slotName) {
    if(slotName.getPrintName().equals(".Data")) {
      return context.evaluate(FunctionCall.newCall(Symbol.get("getDataPart"), object), methods.getMethodsNamespace());
    }
    if(!Types.isS4(object)) {
      SEXP className = object.getAttribute(Symbols.CLASS_NAME);
      if(className.length() == 0) {
        throw new EvalException("trying to get slot \"%s\" from an object of a basic class (\"%s\") with no slots",
                slotName.getPrintName(),
                object.getS3Class().getElementAsString(0));
      } else {
        throw new EvalException("trying to get slot \"%s\" from an object (class \"%s\") that is not an S4 object ",
                slotName.getPrintName(),
                className.getElementAsSEXP(0));
      }
    }

    SEXP value = object.getAttribute(slotName);
    if(value == Null.INSTANCE) {
      if (slotName == Symbol.get(".S3Class")) { /* defaults to class(obj) */
        throw new EvalException("not implemented: .S3Class");
        //return R_data_class(obj, FALSE);
      } else if (slotName == Symbols.NAMES && object instanceof ListVector) {
         /* needed for namedList class */
        return value;
      } else {
        throw new EvalException("cannot get slot %s", slotName);
      }
    }
    if(value == Symbols.S4_NULL) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }


  public static SEXP setElementByName(ListVector list, String name, SEXP value) {
    return setSingleListElementByName(list.newCopyNamedBuilder(), name, value);
  }

  public static SEXP setElementByName(AtomicVector vector, String nameToReplace, SEXP value) {
    // Coerce the atomic vector to a list first
    ListVector.NamedBuilder copyBuilder = ListVector.newNamedBuilder();
    StringVector namesVector = vector.getAttributes().getNames();
    for(int i=0;i!=vector.length();++i) {
      String elementName = "";
      if(namesVector != null) {
        elementName = namesVector.getElementAsString(i);
      }
      copyBuilder.add(elementName, vector.<SEXP>getElementAsSEXP(i));
    }
    return setSingleListElementByName(copyBuilder, nameToReplace, value);
  }

  public static SEXP setElementByName(PairList.Node pairList, String name, SEXP value) {
    return setSingleListElementByName(pairList.newCopyBuilder(), name, value);
  }

  public static SEXP setElementByName(Context context, Environment env, String name, SEXP value) {
    env.setVariable(context, name, value);
    return env;
  }


  /**
   * Tries to set a single element by name in an S4 object.
   *
   * <p>S4 objects that inherit from environments are handled specially. The environment is stored
   * in an .xData attribute, where we have to update the value.</p>
   */
  public static SEXP setElementByName(Context context, S4Object object, String name, SEXP value) {
    SEXP xData = object.getAttribute(Symbols.DOT_XDATA);
    if(!(xData instanceof Environment)) {
      throw new EvalException("object of type 'S4' is not subsettable.");
    }

    // Update environment object *in place*
    setElementByName(context, (Environment)xData, name, value);

    return object;
  }

  /**
   * Same as "[" but not generic
   */
  @Builtin(".subset")
  public static SEXP subset(@Current Context context,
                            SEXP source,
                            @ArgumentList(allowMissing = true) ListVector arguments,
                            @NamedFlag("drop") @DefaultValue(true) boolean drop) {
    Vector vector;
    if(source instanceof Vector) {
      vector = (Vector)source;
    } else if(source instanceof PairList) {
      vector = ((PairList) source).toVector();
    } else {
      throw new EvalException(source.getClass().getName());
    }
    return getSubset(context, vector, arguments, drop);
  }

  @Builtin(".subset2")
  public static SEXP getSingleElementNonGeneric(@Current Context context, SEXP source,
                                                @ArgumentList ListVector subscripts,
                                                @NamedFlag("exact") @DefaultValue(true) boolean exact,
                                                @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    return getSingleElement(context, source, subscripts, exact, drop);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElement(@Current Context context, SEXP source,
                                      @ArgumentList ListVector subscripts,
                                      @NamedFlag("exact") @DefaultValue(true) boolean exact,
                                      @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    // N.B.: the drop argument is accepted but completely ignored

    // If the source is NULL, then no further argument checking is done
    // so if is.null(x) then x[[1,2,3,"foo"]] evaluates happily to NULL
    if(source == Null.INSTANCE) {
      return Null.INSTANCE;
    }

    // Environments can be hidden inside of S4 objects...
    source = Types.unwrapS4Object(source);

    // Environments are handled very differently from vectors, specialize now.
    if(source instanceof Environment) {
      return getSingleEnvironmentElement(context, (Environment) source, subscripts);
    }

    // For the purpose of this operator, convert pairlists to list vectors before continuing
    if(source instanceof PairList) {
      source = ((PairList) source).toVector();
    }

    // A single argument with a length greater than one, like c(1,2,3)
    // are used to index the vector recursively
    if(source instanceof ListVector && isRecursiveIndexingArgument(subscripts)) {

      return getSingleElementRecursively(context, (ListVector) source, (AtomicVector) subscripts.getElementAsSEXP(0), exact, drop);

    } else {

      SelectionStrategy selection = Selections.parseSingleSelection(source, Lists.newArrayList(subscripts));

      if(source instanceof ListVector) {
        return selection.getSingleListElement((ListVector) source, exact);

      } else if(source instanceof AtomicVector) {
        return selection.getSingleAtomicVectorElement((AtomicVector)source, exact);

      } else {
        throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
      }
    }
  }

  private static SEXP getSingleEnvironmentElement(Context context, Environment source, ListVector subscripts) {
    if(subscripts.length() != 1) {
      throw new EvalException("subsetting an environment requires a single argument");
    }
    SEXP subscript = subscripts.get(0);
    if(!(subscript instanceof StringVector) || subscript.length() != 1) {
      throw new EvalException("subset argument for environment must be character of length 1");
    }

    String symbolName = ((StringVector) subscript).getElementAsString(0);

    SEXP value = source.getVariable(context, symbolName);
    if(value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value.force(context);
  }


  private static boolean isRecursiveIndexingArgument(Iterable<SEXP> subscripts) {
    Iterator<SEXP> it = subscripts.iterator();
    if(!it.hasNext()) {
      return false;
    }
    SEXP subscript = it.next();

    return subscript instanceof AtomicVector && subscript.length() > 1;
  }

  private static SEXP getSingleElementRecursively(Context context, ListVector source, AtomicVector indexes, boolean exact, boolean drop) {

    assert indexes.length() > 0;

    SEXP result = source;

    for(int i=0; i < indexes.length(); ++i) {

      if(!(result instanceof Vector)) {
        throw new EvalException("Recursive indexing failed at level %d", i+1);
      }
      result = getSingleElement(context, result, new ListVector(indexes.<SEXP>getElementAsSEXP(i)), exact, drop);
    }
    return result;
  }

  @Generic
  @Builtin("[")
  public static SEXP getSubset(@Current Context context,
                               SEXP source,
                               @ArgumentList(allowMissing = true) ListVector subscripts,
                               @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    if (source == Null.INSTANCE) {
      // handle an exceptional case: if source is NULL,
      // the result is always null
      return Null.INSTANCE;
    }

    ListVector materializedSubscripts = context.materialize(subscripts);

    SelectionStrategy selection = Selections.parseSelection(source, Lists.newArrayList(materializedSubscripts));

    if(source instanceof Vector) {
      return selection.getVectorSubset(context, (Vector) source, drop);

    } else if(source instanceof FunctionCall) {
      return FunctionCall.newCallFromVector(
          (ListVector) selection.getVectorSubset(context, ((FunctionCall) source).toVector(), drop));
      
    } else if(source instanceof PairList.Node) {
      return selection.getVectorSubset(context, ((PairList.Node) source).toVector(), drop);

    } else {
      throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
    }
  }

  @Generic
  @Builtin("[<-")
  public static SEXP setSubset(@Current Context context, SEXP source,
                               @ArgumentList(allowMissing = true) ListVector argumentList) {

    SEXP replacementExp = argumentList.getElementAsSEXP(argumentList.length() - 1);
    if(!(replacementExp instanceof Vector)) {
      throw new EvalException("incompatible types (from %s to %s) in subassignment type fix",
              replacementExp.getTypeName(), source.getTypeName());
    }

    Vector replacement = (Vector) replacementExp;

    // Special case: if both source and replacement have length 0, then return source without
    // even checking subscripts
    if(source.length() == 0 && replacement.length() == 0) {
      return source;
    }

    List<SEXP> subscripts = Lists.newArrayListWithCapacity(argumentList.length() - 1);
    for (int i = 0; i < argumentList.length() - 1; i++) {
      subscripts.add(argumentList.get(i));
    }

    return setSubset(context, source, replacement, subscripts);
  }

  @CompilerSpecialization
  public static SEXP setSubset(Context context, SEXP source, SEXP subscript, SEXP replacement) {
    return setSubset(context, source, (Vector) replacement, Arrays.asList(subscript));
  }

  @CompilerSpecialization
  public static SEXP setSubset(Context context, SEXP source, SEXP subscript1, SEXP subscript2, SEXP replacement) {
    return setSubset(context, source, (Vector) replacement, Arrays.asList(subscript1, subscript2));
  }

  @CompilerSpecialization
  public static SEXP setSubset(Context context, SEXP source, SEXP subscript1, SEXP subscript2, SEXP subscript3, SEXP replacement) {
    return setSubset(context, source, (Vector) replacement, Arrays.asList(subscript1, subscript2, subscript3));
  }

  private static SEXP setSubset(Context context, SEXP source, Vector replacement, List<SEXP> subscripts) {
    SelectionStrategy selection = Selections.parseSelection(source, subscripts);

    if(source instanceof ListVector) {
      return selection.replaceListElements(context, (ListVector) source, replacement);

    } else if(source instanceof FunctionCall) {
      return FunctionCall.newCallFromVector(
          selection.replaceListElements(context, ((PairList.Node) source).toVector(), replacement));

    } else if(source instanceof PairList.Node) {
      return selection.replaceListElements(context, ((PairList.Node) source).toVector(), replacement);

    } else if(source instanceof AtomicVector) {
      return selection.replaceAtomicVectorElements(context, (AtomicVector) source, replacement);

    } else {
      throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
    }
  }


  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(@Current Context context, SEXP source, @ArgumentList ListVector argumentList) {

    // Handle environment case as exceptional first
    if(source instanceof Environment || source instanceof S4Object) {
      return setSingleEnvironmentElement(context, source, argumentList);
    }

    SEXP replacement = argumentList.getElementAsSEXP(argumentList.length() - 1);

    List<SEXP> subscripts = Lists.newArrayListWithCapacity(argumentList.length() - 1);
    for (int i = 0; i < argumentList.length() - 1; i++) {
      subscripts.add(argumentList.get(i));
    }

    if (source instanceof ListVector && isRecursiveIndexingArgument(subscripts) ) {
      return replaceSingleListElementRecursively(context, (ListVector) source, argumentList, replacement);
    }

    SelectionStrategy selection = Selections.parseSingleSelection(source, subscripts);

    if(source instanceof PairList.Node) {
      return selection.replaceSinglePairListElement((PairList.Node) source, replacement);

    } else if(source instanceof ListVector) {
      return selection.replaceSingleListElement((ListVector) source, replacement);

    } else if(source instanceof Null) {
      // Given x[[i]] <- y, where is.null(x), then we create
      // a new atomic vector or list dependin on size of replacement
      if (replacement instanceof AtomicVector && replacement.length() == 1) {
        return selection.replaceSingleElement(context, LogicalVector.EMPTY, (Vector) replacement);
      } else {
        return selection.replaceSingleListElement(new ListVector(), replacement);
      }

    } else if(source instanceof AtomicVector) {
      if(!(replacement instanceof Vector)) {
        throw new EvalException("incompatible types");
      }
      return selection.replaceSingleElement(context, (AtomicVector) source, (Vector) replacement);

    } else {
      throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
    }
  }

  private static SEXP replaceSingleListElementRecursively(Context context, ListVector source, ListVector argumentList, SEXP replacement) {

    SEXP indexes = argumentList.get(0);
    int lastIndex = indexes.length() - 1;
    int leafIndex = lastIndex - 1;

    // create and store the selection strategy (element that needs to be replaced)
    // for each level of (nested) list. loop through lowest to highest level
    SelectionStrategy[] subscriptsArray = new SelectionStrategy[ indexes.length() ];
    for (int i = 0; i <= lastIndex; i++){
      subscriptsArray[i] =  Selections.parseSingleSelection( source, Collections.singletonList(indexes.getElementAsSEXP(i))) ;
    }

    // store separate records for each level of the (nested) list as long its a ListVector
    // throw an error in case you encounter atomic or other types
    Vector[] sources = new Vector[ indexes.length() ];
    sources[0] =  source;
    for (int i = 1; i <= lastIndex; i++){
      SEXP previousSource = sources[i-1];
      if (!(previousSource instanceof ListVector)) {
        throw new EvalException("recursive indexing failed at level " + i);
      }
      sources[i] = (Vector) subscriptsArray[i-1].getSingleListElement( (ListVector) previousSource, false);
    }

    // create new replacement by replacing selected element of highest level ListedVector with
    // replacement, loop through highest to lowest level. In case the highest level is an
    // AtomicVector use the appropriate replace method 'replaceAtomicVectorElements' otherwise use
    // the 'replaceSingleListElement'
    if (sources[lastIndex] instanceof AtomicVector) {
      replacement = subscriptsArray[lastIndex].replaceAtomicVectorElements(context, (AtomicVector) sources[lastIndex], (Vector) replacement);
    } else {
      replacement = subscriptsArray[lastIndex].replaceSingleListElement((ListVector) sources[lastIndex], replacement);
    }

    for (int i = leafIndex ; i >= 0; i--) {
      replacement = subscriptsArray[i].replaceSingleListElement((ListVector) sources[i], replacement);
    }

    return replacement;
  }


  /**
   *  Environment[[name]] <- value
   *
   *  @param source an SEXP of type Environment or an S4 Object which an embedded environment
   *  @param arguments arguments to the [[<- operator
   */
  private static SEXP setSingleEnvironmentElement(Context context, SEXP source, ListVector arguments) {
    if(arguments.length() != 2) {
      throw new EvalException("wrong args for environment subassignment");
    }
    SEXP subscriptExp = arguments.getElementAsSEXP(0);
    SEXP value = arguments.getElementAsSEXP(1);

    if(!(subscriptExp instanceof StringVector) || subscriptExp.length() != 1) {
      throw new EvalException("wrong args for environment subassignment");
    }

    StringVector subscript = (StringVector) subscriptExp;

    String name = subscript.getElementAsString(0);

    if(source instanceof Environment) {
      return setElementByName(context, ((Environment) source), name, value);
    } else if(source instanceof S4Object) {
      return setElementByName(context, ((S4Object) source), name, value);
    } else {
      throw new IllegalArgumentException("Must be environment or S4Object");
    }
  }

  private static SEXP setSingleListElementByName(ListBuilder builder, String nameToReplace, SEXP replacement) {
    int index = builder.getIndexByName(nameToReplace);
    boolean dropDimensions = false;
    
    if(replacement == Null.INSTANCE) {
      if(index != -1) {
        builder.remove(index);
        dropDimensions = true;
      }
    } else {
      if(index == -1) {
        builder.add(nameToReplace, replacement);
        dropDimensions = true;
        
      } else {
        builder.set(index, replacement);
      }
    }
    if(dropDimensions) {
      builder.removeAttribute(Symbols.DIM);
    }
    return builder.build();
  }

  /**
   * Optimized version of {@code setElement} for SEXPs compiled to arrays
   */
  @CompilerSpecialization
  public static double[] setElement(double[] vector, int index, double value) {
    if(index <= 0) {
      return vector;
    }
    double[] copy = Arrays.copyOf(vector, vector.length);
    copy[index - 1] = value;
    return copy;
  }

  @CompilerSpecialization
  public static double[] setElementMutating(double[] vector, int index, double value) {
    if(index <= 0) {
      return vector;
    }
    if(index <= vector.length) {
      vector[index - 1] = value;
      return vector;
    }
    return setElement(vector, index, value);
  }

  @CompilerSpecialization
  public static SEXP setElementMutating(SEXP matrixSexp, int row, int column, double value) {
    if(row <= 0 || column <= 0) {
      return matrixSexp;
    }

    DoubleArrayVector matrix;
    double[] array;
    if(matrixSexp instanceof DoubleArrayVector) {
      matrix = (DoubleArrayVector) matrixSexp;
      array = matrix.toDoubleArrayUnsafe();
    } else {
      array = ((DoubleVector) matrixSexp).toDoubleArray();
      matrix = DoubleArrayVector.unsafe(array, matrixSexp.getAttributes());
    }

    int[] dim = matrix.getAttributes().getDimArray();
    int numRows = dim[0];

    array[(column-1) * numRows + (row-1)] = value;

    return matrix;
  }


  /**
   *
   * @param matrixSexp an SEXP of type {@code double}
   * @param rowIndex 1-based row index
   * @return an array containing the matrix row
   */
  @CompilerSpecialization
  public static double[] getMatrixRow(SEXP matrixSexp, int rowIndex) {
    assert rowIndex >= 1;

    DoubleVector matrix = (DoubleVector) matrixSexp;
    int[] dim = matrix.getAttributes().getDimArray();
    int numRows = dim[0];
    int numCols = dim[1];

    double[] row = new double[numCols];
    int i = rowIndex - 1;
    for (int colIndex = 0; colIndex < numCols; colIndex++) {
      row[colIndex] = matrix.getElementAsDouble(i);
      i += numRows;
    }
    return row;
  }


}
