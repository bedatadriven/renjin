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

package org.renjin.primitives;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
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

  @Internal("comment")
  public static SEXP getComment(SEXP exp) {
    return exp.getAttribute(Symbols.COMMENT);
  }

  @Internal("comment<-")
  public static SEXP setComment(StringVector exp) {
    return exp.setAttribute(Symbols.COMMENT, exp);
  }

  @Builtin("class<-")
  public static SEXP setClass(SEXP exp, Vector classes) {
    return exp.setAttribute("class", classes);

    // TODO:
    // this is apparently more complicated then implemented above:
    // int nProtect = 0;
    // if(isNull(value)) {
    // setAttrib(obj, R_ClassSymbol, value);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // do_unsetS4(obj, value);
    // return obj;
    // }
    // if(TYPEOF(value) != STRSXP) {
    // /* Beware: assumes value is protected, which it is
    // in the only use below */
    // PROTECT(value = coerceVector(duplicate(value), STRSXP));
    // nProtect++;
    // }
    // if(length(value) > 1) {
    // setAttrib(obj, R_ClassSymbol, value);
    // if(IS_S4_OBJECT(obj)) /* multiple strings only valid for S3 objects */
    // do_unsetS4(obj, value);
    // }
    // else if(length(value) == 0) {
    // UNPROTECT(nProtect); nProtect = 0;
    // error(_("invalid replacement object to be a class string"));
    // }
    // else {
    // const char *valueString, *classString; int whichType;
    // SEXP cur_class; SEXPTYPE valueType;
    // valueString = CHAR(asChar(value)); /* ASCII */
    // whichType = class2type(valueString);
    // valueType = (whichType == -1) ? -1 : classTable[whichType].sexp;
    // PROTECT(cur_class = R_data_class(obj, FALSE)); nProtect++;
    // classString = CHAR(asChar(cur_class)); /* ASCII */
    // /* assigning type as a class deletes an explicit class attribute. */
    // if(valueType != -1) {
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // do_unsetS4(obj, value);
    // if(classTable[whichType].canChange) {
    // PROTECT(obj = ascommon(call, obj, valueType));
    // nProtect++;
    // }
    // else if(valueType != TYPEOF(obj))
    // error(_("\"%s\" can only be set as the class if the object has this type; found \"%s\""),
    // valueString, type2char(TYPEOF(obj)));
    // /* else, leave alone */
    // }
    // else if(!strcmp("numeric", valueString)) {
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // do_unsetS4(obj, value);
    // switch(TYPEOF(obj)) {
    // case INTSXP: case REALSXP: break;
    // default: PROTECT(obj = coerceVector(obj, REALSXP));
    // nProtect++;
    // }
    // }
    // /* the next 2 special cases mirror the special code in
    // * R_data_class */
    // else if(!strcmp("matrix", valueString)) {
    // if(length(getAttrib(obj, R_DimSymbol)) != 2)
    // error(_("invalid to set the class to matrix unless the dimension attribute is of length 2 (was %d)"),
    // length(getAttrib(obj, R_DimSymbol)));
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj))
    // do_unsetS4(obj, value);
    // }
    // else if(!strcmp("array", valueString)) {
    // if(length(getAttrib(obj, R_DimSymbol))<= 0)
    // error(_("cannot set class to \"array\" unless the dimension attribute has length > 0"));
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // UNSET_S4_OBJECT(obj);
    // }
    // else { /* set the class but don't do the coercion; that's
    // supposed to be done by an as() method */
    // setAttrib(obj, R_ClassSymbol, value);
    // }
    // }
    // UNPROTECT(nProtect);
    // return obj;

  }

  @Builtin("oldClass<-")
  public static SEXP setOldClass(SEXP exp, Vector classes) {
    /*
     * checkArity(op, args); if (NAMED(CAR(args)) == 2) SETCAR(args,
     * duplicate(CAR(args))); if (length(CADR(args)) == 0) SETCADR(args,
     * R_NilValue); if(IS_S4_OBJECT(CAR(args))) UNSET_S4_OBJECT(CAR(args));
     * setAttrib(CAR(args), R_ClassSymbol, CADR(args)); return CAR(args);
     */
    return exp.setAttribute(Symbols.CLASS, classes);
  }

  @Builtin
  public static SEXP unclass(SEXP exp) {
    return exp.setAttributes(exp.getAttributes().copy().remove(Symbols.CLASS));
  }

  @Builtin("attr<-")
  public static SEXP setAttribute(SEXP exp, String which, SEXP value) {
    return exp.setAttribute(which, value);
  }

  @Builtin
  public static SEXP oldClass(SEXP exp) {
    if (!exp.hasAttributes()) {
      return Null.INSTANCE;
    }
    return exp.getAttribute(Symbols.CLASS);
  }

  @Internal
  public static boolean inherits(SEXP exp, StringVector what) {
    StringVector classes = getClass(exp);
    for (String whatClass : what) {
      if (Iterables.contains(classes, whatClass)) {
        return true;
      }
    }
    return false;
  }

  @Internal
  public static boolean inherits(SEXP exp, String what) {
    return Iterables.contains(getClass(exp), what);
  }

  @Internal
  public static SEXP inherits(SEXP exp, StringVector what, boolean which) {
    if (!which) {
      return new LogicalArrayVector(inherits(exp, what));
    }
    StringVector classes = getClass(exp);
    int result[] = new int[what.length()];

    for (int i = 0; i != what.length(); ++i) {
      result[i] = Iterables.indexOf(classes,
          Predicates.equalTo(what.getElementAsString(i))) + 1;
    }
    return new IntArrayVector(result);
  }
}
