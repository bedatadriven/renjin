/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package org.renjin.sexp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Attributes;


/**
 * Base class for R data types.
 */
public abstract class AbstractSEXP implements SEXP {

  protected AttributeMap attributes;

  private final boolean object;

  protected AbstractSEXP() {
    this.attributes = AttributeMap.EMPTY;
    this.object = false;
  }

  protected AbstractSEXP(AttributeMap attributes) {
    Preconditions.checkNotNull(attributes);
    this.attributes = attributes;
    this.object = attributes.hasClass();
  }

  protected boolean checkDims() {
    Vector dimVector = attributes.getDim();
    if(dimVector.length() == 0) {
      return true;
    }
    int length = 1;
    for(int i=0;i!=dimVector.length();++i) {
      length = length * dimVector.getElementAsInt(i);
    }
    return length == length();
  }


  @Override
  public int length() {
    return 1;
  }

  @Override
  public final boolean hasAttributes() {
    return attributes != AttributeMap.EMPTY;
  }

  @Override
  public AttributeMap getAttributes() {
    return attributes;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  /**
   * Coerces this {@code SEXP} to a single logical value
   * @return
   */
  @Override
  public Logical asLogical() {
    return Logical.NA;
  }
  
  /**
   * Coerces this {@code SEXP} to a single double value.
   */
  @Override
  public double asReal() {
    return DoubleVector.NA;
  }

  /**
   * @return the R language class of this expression
   */
  @Override
  public StringVector getS3Class() {
    SEXP classAttribute = attributes.getClassVector();
    if(classAttribute instanceof StringVector) {
      return (StringVector) classAttribute;
    }
    return new StringArrayVector( getImplicitClass() );
  }
  

  /** 
   * @return  the default class name, to be used if no
   * class attribute is found
   */
  public String getImplicitClass() {
    return getTypeName();
  }

  @Override
  public boolean inherits(String sClassName) {
    if(isObject()) {
      Vector classes = (Vector)getAttribute(Symbols.CLASS);
      for(int i=0;i!=classes.length();++i) {
        if(sClassName.equals(classes.getElementAsString(i))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public AtomicVector getNames() {
    return attributes.getNamesOrNull();
  }

  @Override
  public String getName(int index) {
    if(attributes.hasNames()) {
      StringVector names = attributes.getNames();
      if(names.length() > 0) {
        return names.getElementAsString(index);        
      }
    } 
    return StringVector.NA;
  }

  /**
   * Searches the list of this vector's names for
   * a symbol that matches {@code name}.
   *
   *
   * @param name the name for which to search
   * @return  the index of the matching name, or -1 if
   * no match is found.
   */
  @Override
  public final int getIndexByName(String name) {
    if(attributes != null) {
      SEXP namesExp = attributes.get(Symbols.NAMES);
      if(namesExp instanceof StringVector) {
        StringVector names = (StringVector) namesExp;
        for(int i=0;i!=names.length();++i) {
          if(Objects.equal(names.getElementAsString(i), name)) {
            return i;
          }
        }
      }
    }
    return -1;
  }

  @Override
  public final boolean isObject() {
    return object;
  }

  @Override
  public SEXP getAttribute(Symbol name) {
    return attributes.get(name);
  }

  @Override
  public final SEXP setAttribute(String attributeName, SEXP value) {
     return setAttribute(Symbol.get(attributeName), value);
  }
  
  @Override
  public SEXP setAttribute(Symbol attributeName, SEXP value) {
    return cloneWithNewAttributes(
        replaceAttribute(attributeName,
            Attributes.validateAttribute(this, attributeName, value)));
  }

  @Override
  public SEXP setAttributes(AttributeMap attributes) {
    return cloneWithNewAttributes(attributes);
  }

  private AttributeMap replaceAttribute(Symbol attributeName, SEXP newValue) {
    return this.attributes.copy().set(attributeName, newValue).build();
  }
  
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    if(attributes != AttributeMap.EMPTY) {
      throw new EvalException("cannot change/set attributes on " + getClass().getSimpleName());
    }
    return this;
  }

  @Override
  public String asString() {
    throw new EvalException("Cannot coerce " + getTypeName() + " to scalar string");
  }

  @Override
  public <S extends SEXP> S getElementAsSEXP(int index) {
    if(index == 0) {
      return (S)this;
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  public SEXP force(Context context) {
    return this;
  }
}
