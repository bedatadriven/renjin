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
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Generic;
import org.renjin.primitives.annotations.InvokeAsCharacter;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.vector.RowNamesVector;
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

  /**
   * Validates and possibly reformats special attributes such as {@code class}, {@code names},
   * {@code row.names}.
   * 
   * @param expression the expression on which the attribute is to be set
   * @param name the name of the attribute
   * @param attributeValue the value of the attribute to validate
   * @return {@code attributeValue}, possibly coerced/reformatted 
   * @throws EvalException if the attribute is special, and does not meet exceptions
   */
  public static SEXP validateAttribute(SEXP expression, Symbol name, SEXP attributeValue) {
    if(attributeValue == Null.INSTANCE) {
      return Null.INSTANCE;
    } else if(name.equals(Symbols.CLASS)) {
      return Attributes.validateClassAttributes(attributeValue);
       
    } else if(name.equals(Symbols.NAMES)) {
      return Attributes.validateNamesAttributes(expression, attributeValue);
  
    } else if(name.equals(Symbols.ROW_NAMES)) {
      return Attributes.validateRowNames(attributeValue);

    } else if(name.equals(Symbols.DIM)) {
      return Attributes.validateDim(expression, attributeValue);

    } else {
      return attributeValue;
    }
  }

  public static IntVector validateDim(SEXP sexp, SEXP attributeValue) {

    if(!(attributeValue instanceof Vector)) {
      throw new EvalException("Invalid dim: " + attributeValue);
    }
    Vector vector = (Vector)attributeValue;
    int dim[] = new int[vector.length()];
    int prod = 1;
    for (int i = 0; i != vector.length(); ++i) {
      dim[i] = vector.getElementAsInt(i);
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
   * Validates a {@code names} attribute value 
   * @param expression the expression whose {@code names} attribute is to be updated
   * @param names the proposed {@code names} attribute value
   * @return the {@code names} vector, coerced to a {@link StringVector}
   * @throws EvalException if the given {@code names} vector cannot be coerced to a {@link StringVector} or if it is not
   * the same length as {@code expression} 
   */
  public static StringVector validateNamesAttributes(SEXP expression, SEXP names) {
    if(names.length() > expression.length()) {
      throw new EvalException("'names' attribute [%d] must be the same length as the vector [%d]",
          names.length(), expression.length());
    } else if(!(names instanceof StringVector) || names.length() < expression.length()) {
      return StringArrayVector.coerceFrom(names).setLength(expression.length());

    } else if(names.hasAttributes()) {
      return (StringVector) names.setAttributes(AttributeMap.EMPTY);
    } else {
      return (StringVector) names;
    }
  }

  /**
   * Validates a {@code class} attribute value
   * 
   * @param classNames the proposed {@code class} attribute
   * @return the {@code classNames} vector, coerced to {@link StringVector} if not null.
   */
  public static SEXP validateClassAttributes(SEXP classNames) {
    return classNames.length() == 0 ? Null.INSTANCE : StringArrayVector.coerceFrom(classNames);
  }
  
  /**
   * Validates the {@code row.names} attribute
   * 
   * @param rowNames the {@code row.names} vector to validate
   * @return the given {@code rowNames} vector, possibly in compact form. 
   * @throws EvalException if {@code rowNames} is not a {@link StringVector} or a {@link IntArrayVector}
   */
  public static Vector validateRowNames(SEXP rowNames) {
    
    if(rowNames == Null.INSTANCE) {
      return Null.INSTANCE;
    
    
    // R uses a special "compact format" for row.names that are an integer sequence 1..n
    // in the format c(NA, -n).
    
    } else if(RowNamesVector.isOldCompactForm(rowNames)) {
      return RowNamesVector.fromOldCompactForm(rowNames);
   
    } else if(rowNames instanceof Vector) {
      return (Vector)rowNames;
   
    } 
    
    throw new EvalException("row names must be 'character' or 'integer', not '%s'", rowNames.getTypeName());
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
  @Primitive("dim")
  public static SEXP getDimensions(SEXP sexp) {
    return sexp.getAttribute(Symbols.DIM);
  }

  @Generic
  @Primitive("dim<-")
  public static SEXP setDimensions(SEXP exp, AtomicVector vector) {
    AttributeMap.Builder newAttributes = exp.getAttributes().copy();
    newAttributes.remove(Symbols.NAMES);
    if(vector == Null.INSTANCE) {
      newAttributes.removeDim();
    } else {
      newAttributes.setDim(validateDim(exp, vector));
    }
    return exp.setAttributes(newAttributes.build());
  }

  @Generic
  @Primitive("dimnames")
  public static SEXP getDimensionNames(SEXP exp) {
    return exp.getAttribute(Symbols.DIMNAMES);
  }

  @Generic
  @Primitive("dimnames<-")
  public static SEXP setDimensionNames(@Current Context context, SEXP exp, ListVector dimnames) {
    Vector dim = (Vector) exp.getAttribute(Symbols.DIM);
    if(dim.length() != dimnames.length()) {
      throw new EvalException("length of 'dimnames' [%d] not equal to array extent [%d]",
          dimnames.length(), dim.length());

    }
    ListVector.Builder dn = new ListVector.Builder();
    for(SEXP names : dimnames) {
      if(names != Null.INSTANCE && !(names instanceof StringVector)) {
        names = context.evaluate(FunctionCall.newCall(Symbol.get("as.character"), names));
      }
      dn.add(names);
    }
    return exp.setAttribute(Symbols.DIMNAMES, dn.build());
  }

  @Generic
  @Primitive("dimnames<-")
  public static SEXP setDimensionNames(@Current Context context, SEXP exp, Null nz) {
    return exp.setAttribute(Symbols.DIMNAMES, Null.INSTANCE);
  }

  @Primitive
  public static PairList attributes(SEXP sexp) {
    PairList.Builder pairlist = new PairList.Builder();
    for(Symbol name : sexp.getAttributes().names()) {
      pairlist.add(name, postProcessAttributeValue(name, sexp.getAttributes().get(name)));
    }
    return pairlist.build();
  }

  @Primitive("attr")
  public static SEXP getAttribute(SEXP exp, String which) {
    SEXP partialMatch = null;
    int partialMatchCount = 0;

    AttributeMap attributes = exp.getAttributes();
    for (Symbol name : attributes.names()) {
      if (name.getPrintName().equals(which)) {
        return postProcessAttributeValue(name, attributes.get(name));
      } else if (name.getPrintName().startsWith(which)) {
        partialMatch = postProcessAttributeValue(name, attributes.get(name));
        partialMatchCount++;
      }
    }
    return partialMatchCount == 1 ? partialMatch : Null.INSTANCE;
  }

  @Primitive("attributes<-")
  public static SEXP setAttributes(SEXP exp, ListVector attributes) {
    return setAttributes(exp, attributes.namedValues());
  }

  @Primitive("attributes<-")
  public static SEXP setAttributes(SEXP exp, PairList list) {
    return setAttributes(exp, list.nodes());
  }

  public static SEXP setAttributes(SEXP exp, Iterable<? extends NamedValue> attributes) {
    AttributeMap.Builder builder = AttributeMap.builder();
    for(NamedValue attribute : attributes) {
      Symbol name = Symbol.get(attribute.getName());
      builder.set(name, validateAttribute(exp, name, attribute.getValue()));
    }
    return exp.setAttributes(builder.build());
  }

  @Generic
  @Primitive("names")
  public static SEXP getNames(SEXP exp) {
    // if the vector is a 1-dimensional array,
    // then "names" are stored in the dimnames attribute
    if(exp.getAttributes().getDim().length() == 1) {
      return exp.getAttributes().getDimNames(0);
    }
    return exp.getNames();
  }

  @Generic
  @Primitive("names<-")
  public static SEXP setNames(SEXP exp, @InvokeAsCharacter Vector names) {
    if(exp.getAttributes().getDim().length() == 1) {
      return exp.setAttributes(exp.getAttributes()
          .copy()
          .setArrayNames(names)
          .build());
    } else {
      return exp.setAttribute("names", names);
    }
  }

  @Generic
  @Primitive("levels<-")
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
  @Primitive("class")
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

  @Primitive("comment")
  public static SEXP getComment(SEXP exp) {
    return exp.getAttribute(Symbols.COMMENT);
  }

  @Primitive("comment<-")
  public static SEXP setComment(StringVector exp) {
    return exp.setAttribute(Symbols.COMMENT, exp);
  }

  @Primitive("class<-")
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

  @Primitive("oldClass<-")
  public static SEXP setOldClass(SEXP exp, Vector classes) {
    /*
     * checkArity(op, args); if (NAMED(CAR(args)) == 2) SETCAR(args,
     * duplicate(CAR(args))); if (length(CADR(args)) == 0) SETCADR(args,
     * R_NilValue); if(IS_S4_OBJECT(CAR(args))) UNSET_S4_OBJECT(CAR(args));
     * setAttrib(CAR(args), R_ClassSymbol, CADR(args)); return CAR(args);
     */
    return exp.setAttribute(Symbols.CLASS, classes);
  }

  @Primitive
  public static SEXP unclass(SEXP exp) {
    return exp.setAttributes(exp.getAttributes().copy().remove(Symbols.CLASS).build());
  }

  @Primitive("attr<-")
  public static SEXP setAttribute(SEXP exp, String which, SEXP value) {
    return exp.setAttribute(which, value);
  }

  @Primitive
  public static SEXP oldClass(SEXP exp) {
    if (!exp.hasAttributes()) {
      return Null.INSTANCE;
    }
    return exp.getAttribute(Symbols.CLASS);
  }

  @Primitive
  public static boolean inherits(SEXP exp, StringVector what) {
    StringVector classes = getClass(exp);
    for (String whatClass : what) {
      if (Iterables.contains(classes, whatClass)) {
        return true;
      }
    }
    return false;
  }

  @Primitive
  public static boolean inherits(SEXP exp, String what) {
    return Iterables.contains(getClass(exp), what);
  }

  @Primitive
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
