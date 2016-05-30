/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.methods.MethodDispatch;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

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

  @Builtin("$<-")
  public static SEXP setElementByName(ExternalPtr<?> externalPtr, @Unevaluated SEXP nameExp, SEXP value) {
    externalPtr.setMember(asSymbol(nameExp), value);
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


  @Builtin("$<-")
  public static SEXP setElementByName(ListVector list,
                                      @Unevaluated Symbol name, SEXP value) {
    return setSingleListElementByName(list.newCopyNamedBuilder(), name.getPrintName(), value);
  }

  @Builtin("$<-")
  public static SEXP setElementByName(AtomicVector vector, @Unevaluated Symbol nameToReplace, SEXP value) {
    // Coerce the atomic vector to a list first
    ListVector.NamedBuilder copyBuilder = ListVector.newNamedBuilder();
    StringVector namesVector = vector.getAttributes().getNames();
    for(int i=0;i!=vector.length();++i) {
      String elementName = null;
      if(namesVector != null) {
        elementName = namesVector.getElementAsString(i);
      }
      copyBuilder.add(elementName, vector.getElementAsSEXP(i));
    }
    return setSingleListElementByName(copyBuilder, nameToReplace.getPrintName(), value);
  }

  @Builtin("$<-")
  public static SEXP setElementByName(PairList.Node pairList,
                                      @Unevaluated Symbol name, SEXP value) {
    return setSingleListElementByName(pairList.newCopyBuilder(), name.getPrintName(), value);
  }

  @Builtin("$<-")
  public static SEXP setElementByName(Environment env,
                                      @Unevaluated Symbol name, SEXP value) {
    env.setVariable(name, value);
    return env;
  }

  /**
   * Same as "[" but not generic
   */
  @Builtin(".subset")
  public static SEXP subset(@Current Context context,
                            SEXP source,
                            @ArgumentList ListVector arguments,
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
  public static SEXP getSingleElementNonGeneric(SEXP source, @ArgumentList ListVector subscripts,
                                                @NamedFlag("exact") @DefaultValue(true) boolean exact,
                                                @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    return getSingleElement(source, subscripts, exact, drop);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElement(SEXP source,
                                      @ArgumentList ListVector subscripts,
                                      @NamedFlag("exact") @DefaultValue(true) boolean exact,
                                      @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    // N.B.: the drop argument is accepted but completely ignored

    // If the source is NULL, then no further argument checking is done
    // so if is.null(x) then x[[1,2,3,"foo"]] evaluates happily to NULL
    if(source == Null.INSTANCE) {
      return Null.INSTANCE;
    }

    // Environments are handled very differently from vectors, specialize now.
    if(source instanceof Environment) {
      return getSingleEnvironmentElement((Environment) source, subscripts);
    }

    // For the purpose of this operator, convert pairlists to list vectors before continuing
    if(source instanceof PairList) {
      source = ((PairList) source).toVector();
    }

    // A single argument with a length greater than one, like c(1,2,3)
    // are used to index the vector recursively
    if(source instanceof ListVector && isRecursiveIndexingArgument(subscripts)) {

      return getSingleElementRecursively((ListVector) source, (AtomicVector) subscripts.getElementAsSEXP(0), exact, drop);

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

  private static SEXP getSingleEnvironmentElement(Environment source, ListVector subscripts) {
    if(subscripts.length() != 1) {
      throw new EvalException("subsetting an environment requires a single argument");
    }
    SEXP subscript = subscripts.get(0);
    if(!(subscript instanceof StringVector) || subscript.length() != 1) {
      throw new EvalException("subset argument for environment must be character of length 1");
    }

    String symbolName = ((StringVector) subscript).getElementAsString(0);
    if(StringVector.isNA(symbolName)) {
      throw new EvalException("subset argument for environment cannot be NA");
    }

    SEXP value = source.getVariable(symbolName);
    if(value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }


  private static boolean isRecursiveIndexingArgument(Iterable<SEXP> subscripts) {
    Iterator<SEXP> it = subscripts.iterator();
    if(!it.hasNext()) {
      return false;
    }
    SEXP subscript = it.next();

    return subscript instanceof AtomicVector && subscript.length() > 1;
  }

  private static SEXP getSingleElementRecursively(ListVector source, AtomicVector indexes, boolean exact, boolean drop) {

    assert indexes.length() > 0;

    SEXP result = source;

    for(int i=0; i < indexes.length(); ++i) {

      if(!(result instanceof Vector)) {
        throw new EvalException("Recursive indexing failed at level %d", i+1);
      }
      result = getSingleElement(result, new ListVector(indexes.getElementAsSEXP(i)), exact, drop);
    }
    return result;
  }

  @Generic
  @Builtin("[")
  public static SEXP getSubset(@Current Context context,
                               SEXP source,
                               @ArgumentList ListVector subscripts,
                               @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    if (source == Null.INSTANCE) {
      // handle an exceptional case: if source is NULL,
      // the result is always null
      return Null.INSTANCE;
    }

    SelectionStrategy selection = Selections.parseSelection(source, Lists.newArrayList(subscripts));

    if(source instanceof Vector) {
      return selection.getVectorSubset(context, (Vector) source, drop);

    } else if(source instanceof FunctionCall) {
      return selection.getFunctionCallSubset((FunctionCall) source);

    } else if(source instanceof PairList.Node) {
      return selection.getVectorSubset(context, ((PairList.Node) source).toVector(), drop);

    } else {
      throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
    }
  }


  @Generic
  @Builtin("[<-")
  public static SEXP setSubset(@Current Context context, SEXP source, @ArgumentList ListVector argumentList) {

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

    SelectionStrategy selection = Selections.parseSelection(source, subscripts);

    if(source instanceof ListVector) {
      return selection.replaceListElements(context, (ListVector) source, replacement);

    } else if(source instanceof PairList.Node) {
      return selection.replaceListElements(context, ((PairList.Node) source).toVector(), replacement);

    } else if(source instanceof AtomicVector) {
      return selection.replaceAtomicVectorElements(context, (AtomicVector) source, replacement);

    } else {
      throw new EvalException("object of type '%' is not subsettable", source.getTypeName());
    }
  }


  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(@Current Context context, SEXP source, @ArgumentList ListVector argumentList) {

    // Handle environment case as exceptional first
    if(source instanceof Environment) {
      return setSingleEnvironmentElement((Environment) source, argumentList);
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
        return selection.replaceSingleElement(LogicalVector.EMPTY, (Vector) replacement);
      } else {
        return selection.replaceSingleListElement(new ListVector(), replacement);
      }

    } else if(source instanceof AtomicVector) {
      if(!(replacement instanceof Vector)) {
        throw new EvalException("incompatible types");
      }
      return selection.replaceSingleElement((AtomicVector) source, (Vector) replacement);

    } else {
      throw new EvalException("object of type '%s' is not subsettable");
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
   */
  private static Environment setSingleEnvironmentElement(Environment source, ListVector arguments) {
    if(arguments.length() != 2) {
      throw new EvalException("wrong args for environment subassignment");
    }
    SEXP subscriptExp = arguments.getElementAsSEXP(0);
    SEXP value = arguments.getElementAsSEXP(1);

    if(!(subscriptExp instanceof StringVector) || subscriptExp.length() != 1) {
      throw new EvalException("wrong args for environment subassignment");
    }

    StringVector subscript = (StringVector) subscriptExp;

    source.setVariable(subscript.getElementAsString(0), value);

    return source;
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

}
