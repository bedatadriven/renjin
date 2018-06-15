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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.s4.S4;
import org.renjin.sexp.*;

/**
 * 
 * Attributes are an R language feature that allow metadata to be attached to 
 * R-language expressions.
 * 
 * Certain attributes have special meaning and their content is needs to be consistently enforced.
 *
 */
public class Attributes {


  private Attributes() {}


  public static IntVector validateDim(SEXP sexp, SEXP attributeValue) {

    if(!(attributeValue instanceof Vector)) {
      throw new EvalException("Invalid dim: " + attributeValue);
    }
    Vector vector = (Vector)attributeValue;
    int dim[] = new int[vector.length()];
    int prod = 1;
    for (int i = 0; i != vector.length(); ++i) {
      dim[i] = vector.getElementAsInt(i);
      if(dim[i] < 0) {
        throw new EvalException("the dims contain negative values");
      }
      prod *= dim[i];
    }

    if (prod != sexp.length()) {
      throw new EvalException(
          "dims [product %d] do not match the length of object [%d]", prod,
          sexp.length());
    }

    return new IntArrayVector(dim);
  }


  /**
   * Expands attributes for 'public' consumption. 
   * 
   * Some attributes (only {@code row.names} as far as I know at this point) are stored in 
   * internal compact forms and need to be expanded before being handed to the user.
   * 
   * @param attributes internal attributes pairlist.
   * @return an expanded attributes pairlist.
   */
  public static PairList expandAttributes(PairList attributes) {
    PairList.Builder result = new PairList.Builder();
    for(PairList.Node node : attributes.nodes()) {
      result.add(node.getTag(), postProcessAttributeValue(node.getTag(), node.getValue()));
    }
    return result.build();
  }
  
  public static SEXP postProcessAttributeValue(Symbol name, SEXP value) {
    return value;
  }

  @Generic
  @Builtin("dim")
  public static SEXP getDimensions(SEXP sexp) {
    return sexp.getAttribute(Symbols.DIM);
  }

  @Generic
  @Builtin("dim<-")
  public static SEXP setDimensions(SEXP exp, AtomicVector vector) {
    
    if((!(exp instanceof Vector) && 
        !(exp instanceof PairList)) || exp instanceof FunctionCall) {
      throw new EvalException("cannot set dim() on object of type '%s'", exp.getTypeName());
    }
    
    AttributeMap.Builder newAttributes = exp.getAttributes().copy();
    if(vector == Null.INSTANCE) {
      newAttributes.removeDim();
    } else {
      newAttributes.setDim(vector);
    }

    // Always remove names attribute
    newAttributes.remove(Symbols.NAMES);

    // ALWAYS drop dimnames, attribute, even if dimensions haven't changed
    newAttributes.removeDimnames();

    return exp.setAttributes(newAttributes);
  }

  @Generic
  @Builtin("dimnames")
  public static SEXP getDimensionNames(SEXP exp) {
    return exp.getAttribute(Symbols.DIMNAMES);
  }

  @Generic
  @Builtin("dimnames<-")
  public static SEXP setDimensionNames(@Current Context context, SEXP exp, ListVector dimnames) {
    
    if(dimnames.length() == 0) {
      return exp.setAttribute(Symbols.DIMNAMES, Null.INSTANCE);
    }

    // Convert the list to character vectors
    ListVector.Builder dn = new ListVector.Builder();
    dn.setAttribute(Symbols.NAMES, dimnames.getNames());
    for(SEXP names : dimnames) {
      if(names != Null.INSTANCE && !(names instanceof StringVector)) {
        names = context.evaluate(FunctionCall.newCall(Symbol.get("as.character"), names));
      }
      dn.add(names);
    }
    
    return exp.setAttribute(Symbols.DIMNAMES, dn.build());
  }

  @Generic
  @Builtin("dimnames<-")
  public static SEXP setDimensionNames(@Current Context context, SEXP exp, Null nz) {
    return exp.setAttribute(Symbols.DIMNAMES, Null.INSTANCE);
  }

  @Builtin
  public static Vector attributes(SEXP sexp) {
    AttributeMap attributes = sexp.getAttributes();
    if(attributes == AttributeMap.EMPTY) {
      return Null.INSTANCE;
    } else {
      ListVector.NamedBuilder list = new ListVector.NamedBuilder();
      for (Symbol name : attributes.names()) {
        list.add(name, postProcessAttributeValue(name, attributes.get(name)));
      }
      return list.build();
    }
  }

  @Builtin("attr")
  public static SEXP getAttribute(SEXP exp, String which, boolean exact) {
    SEXP partialMatch = null;
    int partialMatchCount = 0;

    AttributeMap attributes = exp.getAttributes();
    for (Symbol name : attributes.names()) {
      if (name.getPrintName().equals(which)) {
        return postProcessAttributeValue(name, attributes.get(name));
      } else if (!exact && name.getPrintName().startsWith(which)) {
        partialMatch = postProcessAttributeValue(name, attributes.get(name));
        partialMatchCount++;
      }
    }
    return partialMatchCount == 1 ? partialMatch : Null.INSTANCE;
  }
  
  @Builtin("attr")
  public static SEXP getAttribute(SEXP exp, String which) {
    return getAttribute(exp, which, false);
  }

  @Builtin("attributes<-")
  public static SEXP setAttributes(SEXP exp, ListVector attributes) {
    return setAttributes(exp, attributes.namedValues());
  }

  @Builtin("attributes<-")
  public static SEXP setAttributes(SEXP exp, PairList list) {
    return setAttributes(exp, list.nodes());
  }

  public static SEXP setAttributes(SEXP exp, Iterable<? extends NamedValue> attributes) {
    AttributeMap.Builder builder = AttributeMap.builder();
    for(NamedValue attribute : attributes) {
      Symbol name = Symbol.get(attribute.getName());
      builder.set(name, attribute.getValue());
    }
    if(exp == Null.INSTANCE) {
      return ListVector.EMPTY.setAttributes(builder);
    } else {
      return exp.setAttributes(builder);
    }
  }

  @Generic
  @Builtin("names")
  public static SEXP getNames(SEXP exp) {
    // if the vector is a 1-dimensional array,
    // then "names" are stored in the dimnames attribute
    if(exp.getAttributes().getDim().length() == 1) {
      return exp.getAttributes().getDimNames(0);
    }
    return exp.getNames();
  }

  @Generic
  @Builtin("names<-")
  public static SEXP setNames(@Current Context context, SEXP exp, @InvokeAsCharacter Vector names) {
    
    // Verify that setting the names on this object is legal
    if(Types.isS4(exp)) {
      String className = ((StringVector) exp.getAttribute(Symbols.CLASS)).getElementAsString(0);

      if (exp instanceof S4Object) {
        // The names() function can never be used to assign the names slot on a "real" S4 object
        if (exp.getAttribute(Symbols.NAMES) == Null.INSTANCE) {
          throw new EvalException("class '%s' has no 'names' slot", className);
        } else {
          throw new EvalException("invalid to use names()<- to set the 'names' slot in a non-vector class ('%s')",
                  className);
        }
      } else {
        // However, it IS legal to use names() to assign the slot on a vector that has been baptized as an S4 object
        // We do warn if the class does not have a names slot
        if (exp.getAttribute(Symbols.NAMES) == Null.INSTANCE) {
          context.warn(String.format(
                  "class '%s' has no 'names' slot; assigning a names attribute will create an invalid object", className));
        }
      }
    }
    
    if(exp.getAttributes().getDim().length() == 1) {
      return exp.setAttributes(exp.getAttributes()
          .copy()
          .setArrayNames(names));
    }

    AttributeMap.Builder newAttributes = exp.getAttributes().copy();
    newAttributes.setNames(names);
    
    return exp.setAttributes(newAttributes);
  }

  @Generic
  @Builtin("levels<-")
  public static SEXP setLabels(SEXP exp, SEXP levels) {
    return exp.setAttribute(Symbols.LEVELS, levels);
  }

  /**
   *
   * This implements the 'class' builtin. The R docs mention this function in
   * the context of S3 dispatch, but it appears that the logic has diverged:
   * class(9) for example will return 'numeric', but the class list used for
   * dispatch by UseMethod is actually c('double', 'numeric')
   *
   * @param exp
   * @return
   */
  @Builtin("class")
  public static StringVector getClass(SEXP exp) {

    SEXP classAttribute = exp.getAttribute(Symbols.CLASS);
    if (classAttribute.length() > 0) {
      return (StringVector) classAttribute;
    }

    SEXP dim = exp.getAttribute(Symbols.DIM);
    if (dim.length() == 2) {
      return StringVector.valueOf("matrix");
    } else if (dim.length() > 0) {
      return StringVector.valueOf("array");
    }

    return StringVector.valueOf(exp.getImplicitClass());
  }

  @Builtin
  public static SEXP unclass(SEXP exp) {
    return exp.setAttributes(exp.getAttributes().copy().remove(Symbols.CLASS));
  }

  @Builtin("attr<-")
  public static SEXP setAttribute(SEXP exp, String which, SEXP value) {
    return exp.setAttribute(which, value);
  }

  @Internal
  public static SEXP inherits(@Current Context context, SEXP exp, StringVector what, boolean which) {

    StringVector classes = getClass(exp);
    boolean s4 = Types.isS4(exp);

    int inherits[] = new int[what.length()];
    for (int i = 0; i < what.length(); i++) {
      String whatClass = what.getElementAsString(i);
      inherits[i] = inherits(context, whatClass, classes, s4);
    }

    if (which) {
      return new IntArrayVector(inherits);
    } else {
      for (int i = 0; i < inherits.length; i++) {
        if(inherits[i] != 0) {
          return LogicalVector.TRUE;
        }
      }
      return LogicalVector.FALSE;
    }
  }

  private static int inherits(Context context, String whatClass, StringVector classes, boolean s4) {
    for (int i = 0; i < classes.length(); i++) {
      String className = classes.getElementAsString(i);
      if(whatClass.equals(className)) {
        return i + 1;
      } else if(s4) {
        AtomicVector superClasses = S4.computeDataClassesS4(context, className);
        for (int j = 0; j < superClasses.length(); j++) {
          if(whatClass.equals(superClasses.getElementAsString(j))) {
            return i + 1;
          }
        }
      }
    }
    return 0;
  }

}
