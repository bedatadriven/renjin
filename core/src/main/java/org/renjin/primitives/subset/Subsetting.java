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

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.methods.MethodDispatch;
import org.renjin.primitives.Types;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

import java.util.List;

public class Subsetting {

  enum Type {
    SINGLE_BRACKET,
    DOUBLE_BRACKET
  }
  
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
  public static SEXP subset(SEXP source, @ArgumentList ListVector arguments,
                            @NamedFlag("drop") @DefaultValue(true) boolean drop) {
    Vector vector;
    if(source instanceof Vector) {
      vector = (Vector)source;
    } else if(source instanceof PairList) {
      vector = ((PairList) source).toVector();
    } else {
      throw new EvalException(source.getClass().getName());
    }
    return getSubset(vector, arguments, drop);
  }

  @Builtin(".subset2")
  public static SEXP getSingleElementNonGeneric(SEXP source, @ArgumentList ListVector subscripts,
                                                @NamedFlag("exact") @DefaultValue(true) boolean exact,
                                                @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    return getSingleElement(source, subscripts, exact, drop);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElement(SEXP source, @ArgumentList ListVector subscripts,
                                      @NamedFlag("exact") @DefaultValue(true) boolean exact,
                                      @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    // N.B.: the drop argument is completely ignored

    if(source == Null.INSTANCE) {
      return source;
    }

    String nameSubscript = isSingleStringSubscript(subscripts);
    if(nameSubscript != null) {
      return getSingleElementByName(source, nameSubscript, exact);
    }

    if(source instanceof PairList) {
      source = ((PairList) source).toVector();
    }

    // A single argument with a length greater than one, like c(1,2,3)
    // are used to index the vector recursively
    if(source instanceof ListVector && isRecursiveIndexingArgument(subscripts)) {

      return getSingleElementRecursively((ListVector) source, (AtomicVector) subscripts.getElementAsSEXP(0), exact, drop);

    } else {

      return new SubscriptOperation()
          .setSource(source, subscripts)
          .setDrop(true)
          .extractSingle();
    }
  }

  private static boolean isRecursiveIndexingArgument(ListVector subscripts) {
    if(subscripts.length() != 1) {
      return false;
    }
    SEXP subscript = subscripts.getElementAsSEXP(0);
    if(!(subscript instanceof AtomicVector)) {
      return false;
    }
    return subscript.length() > 1;
  }

  private static SEXP getSingleElementRecursively(ListVector source, AtomicVector indexes, boolean exact, boolean drop) {

    assert indexes.length() > 0;

    SEXP result = source;

    for(int i=0; i < indexes.length(); ++i) {

      if(!(result instanceof ListVector)) {
        throw new EvalException("Recursive indexing failed at level %d", i+1);
      }
      result = getSingleElement(result, new ListVector(indexes.getElementAsSEXP(i)), exact, drop);
    }
    return result;
  }

  private static String isSingleStringSubscript(ListVector subscripts) {
    if(subscripts.length() != 1) {
      return null;
    }
    SEXP subscript = subscripts.getElementAsSEXP(0);
    if(subscript instanceof StringVector && subscript.length() == 1) {
      return ((StringVector) subscript).getElementAsString(0);
    } else {
      return null;
    }
  }

  private static SEXP getSingleElementByName(SEXP source, String subscript, boolean exact) {
    if(source instanceof Environment) {
      return getSingleEnvironmentVariable((Environment) source, subscript, exact);
    } else if(source instanceof PairList) {
      return getSingleVectorElement(((PairList) source).toVector(), subscript, exact);
    } else if(source instanceof Vector) {
      return getSingleVectorElement((Vector)source, subscript, exact);
    }
    throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
  }

  private static SEXP getSingleVectorElement(Vector source, String subscript, boolean exact) {
    Vector names = source.getNames();
    if(names == Null.INSTANCE) {
      return Null.INSTANCE;
    } else {
      // do a full pass through to check for exact matches, otherwise
      // return the first partial match if exact==FALSE
      SEXP partialMatch = Null.INSTANCE;
      int matchCount = 0;
      for(int i=0;i!=names.length();++i) {
        if(!names.isElementNA(i)) {
          if (names.getElementAsString(i).equals(subscript)) {
            return source.getElementAsSEXP(i);
          } else if (!exact && names.getElementAsString(i).startsWith(subscript)) {
            matchCount++;
            partialMatch = source.getElementAsSEXP(i);
          }
        }
      }
      if(matchCount == 1) {
        return partialMatch;
      } else {
        return Null.INSTANCE;
      }
    }
  }

  private static SEXP getSingleEnvironmentVariable(Environment source, String subscript, boolean exact) {
    SEXP value;
    if(exact) {
      value = source.getVariable(subscript);
    } else {
      value = source.getVariableByPrefix(subscript);
    }
    if(value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }

  @Generic
  @Builtin("[")
  public static SEXP getSubset(SEXP source, @ArgumentList ListVector subscripts,
                               @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    if (source == Null.INSTANCE) {
      // handle an exceptional case: if source is NULL,
      // the result is always null
      return Null.INSTANCE;

    } else if(source instanceof FunctionCall) {
      return getCallSubset((FunctionCall) source, subscripts);

    } else if(source instanceof PairList.Node) {
      return getSubset(((PairList.Node) source).toVector(), subscripts, drop);

    } else if(source instanceof Vector) {
      return getSubset((Vector)source, subscripts, drop);

    } else {
      throw new EvalException("object of type '%s' is not subsettable", source.getTypeName());
    }
  }

  private static SEXP getCallSubset(FunctionCall source, ListVector subscripts) {
    Selection selection = new VectorIndexSelection(source, subscripts.get(0));
    FunctionCall.Builder call = FunctionCall.newBuilder();
    call.withAttributes(source.getAttributes());

    IndexIterator it = selection.iterator();
    while(it.hasNext()) {
      int sourceIndex = it.next();
      call.addCopy(source.getNode(sourceIndex));
    }
    return call.build();
  }

  private static SEXP getSubset(Vector source, ListVector subscripts, boolean drop) {
    
    Selection2 selection = Selections.parseSelection(source, Lists.newArrayList(subscripts));
    
    return selection.get(source, drop);
    
  }

  @Generic
  @Builtin("[<-")
  public static SEXP setSubset(SEXP source, @ArgumentList ListVector argumentList) {

    SEXP replacementExp = argumentList.getElementAsSEXP(argumentList.length() - 1);
    if(!(replacementExp instanceof Vector)) {
      throw new EvalException("incompatible types (from %s to %s) in subassignment type fix",
          replacementExp.getTypeName(), source.getTypeName());
    }
    
    Vector replacement = (Vector) replacementExp;
    
    List<SEXP> subscripts = Lists.newArrayListWithCapacity(argumentList.length() - 1);
    for (int i = 0; i < argumentList.length() - 1; i++) {
      subscripts.add(argumentList.get(i));
    }
    
    Selection2 selection = Selections.parseSelection(source, subscripts);

    if(source instanceof ListVector) {
      return selection.replaceListElements((ListVector) source, replacement);
    
    } else if(source instanceof PairList.Node) {
      return selection.replaceListElements(((PairList.Node) source).toVector(), replacement);
      
    } else if(source instanceof AtomicVector) {

      return selection.replaceAtomicVectorElements((AtomicVector) source, replacement);
    
    } else if(source == Null.INSTANCE) {
      throw new EvalException("invalid (NULL) left side of assignment");
    
    } else {
      throw new EvalException("object of type '%' is not subsettable", source.getTypeName());
    }
  }


  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(SEXP source, @ArgumentList ListVector argumentList) {

    // Handle environment case as exceptional first
    if(source instanceof Environment) {
      return setSingleEnvironmentElement((Environment) source, argumentList);
    }

    SEXP replacement = argumentList.getElementAsSEXP(argumentList.length() - 1);

    List<SEXP> subscripts = Lists.newArrayListWithCapacity(argumentList.length() - 1);
    for (int i = 0; i < argumentList.length() - 1; i++) {
      subscripts.add(argumentList.get(0));
    }

    Selection2 selection = Selections.parseSelection(source, subscripts);
    

    if(source instanceof PairList.Node) {
      return selection.replaceSinglePairListElement((PairList.Node) source, replacement);

    } else if(source instanceof ListVector) {
      return selection.replaceSingleListElement((ListVector) source, replacement);

    } else if(source instanceof Null) {
      // Given x[[i]] <- y, where is.null(x), then we create
      // a new list...
      return selection.replaceSingleListElement(new ListVector(), replacement);
      
    } else if(source instanceof AtomicVector) {
      if(!(replacement instanceof Vector)) {
        throw new EvalException("incompatible types");
      }
      return selection.replaceSingleElement((AtomicVector) source, (Vector) replacement);

    } else {
      throw new EvalException("object of type '%s' is not subsettable");
    }
  }



  /**
   * Builds a {@link Selection} object from a list of subscript arguments for the {@code [} operator
   */
  public static Selection parseSelection(SEXP source, List<SEXP> subscripts) {

    if(subscripts.isEmpty()) {
      return new CompleteSelection(source);

    } else if(subscripts.size() == 1) {

      SEXP subscript = subscripts.get(0);

      // if the single argument is a matrix or greater, then
      // we treat it as a matrix of coordinates
      if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {

        return new CoordinateMatrixSelection(source, subscript);

      } else {

        // otherwise we treat the source
        // as a vector, regardless of whether it has dimensions or not

        return new VectorIndexSelection(source, subscript);
      }

    } else {

      // otherwise we have multiple subscripts, and we treat each subscript
      // as applying as whole to its dimensions (including whole rows or columns
      // in the case of matrices)

      return new DimensionSelection(source, subscripts);
    }
  }
  

  /**
   * Builds a {@link Selection} object from a list of subscript arguments for the {@code [[} operator
   */
  public static Selection parseSingleSelection(SEXP source, List<SEXP> subscripts) {

    if(subscripts.isEmpty()) {
      throw new EvalException("[[]] with missing subscript");

    } else if(subscripts.size() == 1) {
      // NOTE: 
      // Subscripts are interpreted differently within the context
      // of the [[i]] operator:
      // - coordinate matrix subscripts are NOT permitted
      // - logical subscripts (TRUE, FALSE) are treated as integer subscripts

      SEXP subscriptExp = subscripts.get(0);
      Subscript subscript;
      if(subscriptExp instanceof StringVector) {
        subscript = new NamedSubscript((Vector)source, (StringVector) subscriptExp);

      } else if(subscriptExp instanceof DoubleVector ||
                subscriptExp instanceof IntVector) {
        
        subscript = new PositionalSubscript((AtomicVector)subscriptExp);

      } else {
        throw new EvalException("invalid subscript type '%s'", subscriptExp.getTypeName());
      }
      return new VectorIndexSelection(source, subscript);


    } else {
      return new DimensionSelection(source, subscripts);
    }
  }


  /**
   * Removes selected elements from a list vector.
   */
  private static ListVector removeListElements(ListVector source, Selection selection) {

    IndexPredicate predicate = selection.computePredicate();

    ListVector.Builder newList = new ListVector.Builder();

    // Copy elements, minus any selected elements
    for(int i=0;i<source.length();++i) {
      if(!predicate.apply(i)) {
        newList.add(source.getElementAsSEXP(i));
      }
    }

    // Build the attributes first
    for (Symbol attributeName : source.getAttributes().names()) {
      if(attributeName == Symbols.NAMES) {
        // Build the names vector, applying our filter
        AtomicVector names = source.getNames();
        StringArrayVector.Builder newNames = new StringArrayVector.Builder();
        for(int i=0;i<names.length();++i) {
          if(predicate.apply(i)) {
            newNames.add(names.getElementAsString(i));
          }
        }
        newList.setAttribute(Symbols.NAMES, newNames.build());
      } else if(attributeName == Symbols.DIM) {
        // drop dims, we are treating the list as a vector
        // now, not a matrix

      } else {
        // preserve other attributes
        newList.setAttribute(attributeName, source.getAttribute(attributeName));
      }
    }
    return newList.build();
  }


  private static Vector setSubset(Vector source, Selection selection, Vector elements) {

    if(selection.isByName()) {
      return setSubsetByName(source, selection, elements);
    }
    if(!selection.isEmpty() && elements.length() == 0) {
      throw new EvalException("replacement has zero length");
    }

    Vector.Builder result = createReplacementBuilder(source, elements);

    int replacement = 0;
    IndexIterator it = selection.iterator();
    while(it.hasNext()) {
      int index = it.next();
      assert index < source.length() || selection.getSourceDimensions() == 1;
      if(!IntVector.isNA(index)) {
        result.setFrom(index, elements, replacement++);
        if(replacement >= elements.length()) {
          replacement = 0;
        }
      }
    }
    return result.build();
  }

  private static Vector.Builder createReplacementBuilder(Vector source, SEXP elements) {
    Vector.Builder result;

    Vector.Type replacementType;
    if(elements instanceof AtomicVector) {
      replacementType = ((AtomicVector) elements).getVectorType();
    } else {
      replacementType = ListVector.VECTOR_TYPE;
    }

    if(source.getVectorType().isWiderThanOrEqualTo(replacementType)) {
      result = source.newCopyBuilder();
    } else {
      result = replacementType.newBuilderWithInitialSize(source.length());
      result.copyAttributesFrom(source);
      for(int i=0;i!= source.length();++i) {
        result.setFrom(i, source, i);
      }
    }
    return result;
  }

  private static Vector setSubsetByName(Vector source, Selection selection, SEXP replacement) {
    StringVector namesToReplace = selection.getNames();
    Vector.Builder result = createReplacementBuilder(source, replacement);
    NamesBuilder names = NamesBuilder.clonedFrom(source);
    
    boolean dropDimensions = false;

    int replacementIndex = 0;

    for(String nameToReplace : namesToReplace) {
      int index = source.getIndexByName(nameToReplace);
      if(index == -1) {
        index = result.length();
        names.set(index, nameToReplace);
        dropDimensions = true;
      }
    
      result.setFrom(index, replacement, replacementIndex++);
      
      if(replacementIndex >= replacement.length()) {
        replacementIndex = 0;
      }
    }

    result.setAttribute(Symbols.NAMES, names.build());
    if(dropDimensions) {
      result.removeAttribute(Symbols.DIM);
    }
    return result.build();
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
  
  private static SEXP setSingleAtomicVectorElement(AtomicVector source, Selection selection, SEXP replacement) {
    if(selection.isByName()) {
      return setSubsetByName(source, selection, replacement);
    } else {
      return setSingleElementByIndex(source, selection, replacement);
    }

  }

  private static Vector setSingleElementByIndex(Vector source, Selection selection, SEXP replacement) {
    // Verify that we've selected a single element
    int index = getExactlyOneIndex(selection);

    Vector.Builder newVector = createReplacementBuilder(source, replacement);
    if(source instanceof ListVector) {
      newVector.set(index, replacement);
    } else {
      if(replacement.length() != 1) {
        throw new EvalException("more elements supplied than there are to replace");
      }
      newVector.setFrom(index, replacement, 0);
    }
    return newVector.build();
  }
  
  private static SEXP setSingleListElement(ListBuilder builder, Selection selection, SEXP replacement) {
    if(selection.isByName()) {
      return setSingleListElementByName(builder, selection.getNames().getElementAsString(0), replacement);
    }

    int index = getExactlyOneIndex(selection);
    
    if(replacement == Null.INSTANCE) {
      builder.remove(index);
    } else {
      builder.set(index, replacement);
    }
    
    return builder.build();
  }

  private static int getExactlyOneIndex(Selection selection) {
    IndexIterator it = selection.iterator();
    if(!it.hasNext()) {
      throw new EvalException("attempt to select less than one element");
    }
    int index = it.next();
    if(it.hasNext()) {
      throw new EvalException("attempt to select more than one element");
    }
    return index;
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
