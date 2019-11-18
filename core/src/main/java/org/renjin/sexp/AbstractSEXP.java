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
package org.renjin.sexp;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.base.Preconditions;

import java.util.Objects;


/**
 * Base class for R data types.
 */
public abstract class AbstractSEXP implements SEXP {

  private AttributeMap attributes;

  private boolean object;

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
  public void unsafeSetLength(int length) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support unsafeSetLength");
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

  @Override
  public int asInt() {
    return IntVector.NA;
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
    // In the very special case of a one-dimensional array,
    // the names of the elements are stored in the dimnames attribute
    // and not the names attribute.
    if(attributes.getDim().length() == 1) {
      return attributes.getDimNames(0);
    } else {
      return attributes.getNamesOrNull();
    }
  }

  @Override
  public boolean hasNames() {
    return getNames() instanceof StringVector;
  }

  @Override
  public String getName(int index) {
    if(hasNames()) {
      return getNames().getElementAsString(index);
    } 
    return "";
  }

  public boolean hasName(int i) {
    return !"".equals(getName(i));
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
          if(Objects.equals(names.getElementAsString(i), name)) {
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
    if(this instanceof S4Object) {
      return setAttributes(this.attributes.copyS4().set(attributeName, value));
    }
    return setAttributes(this.attributes.copy().set(attributeName, value));
  }

  @Override
  public SEXP setAttributes(AttributeMap attributes) {
    return cloneWithNewAttributes(attributes);
  }

  @Override
  public SEXP setAttributes(AttributeMap.Builder attributes) {
    return cloneWithNewAttributes(attributes.validateAndBuildForVectorOfLength(length()));
  }

  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    if(attributes != AttributeMap.EMPTY) {
      throw new EvalException("cannot change/set attributes on " + getClass().getName());
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

  @Override
  public SEXP eval(Context context, Environment rho) {
    context.clearInvisibleFlag();
    return this;
  }

  /**
   * Modifies this SEXP's attributes in place. Some {@code SEXP}s
   * MAY be shared between multiple threads and so are assumed immutable. Modifications to the
   * array should ONLY be undertaken very carefully and when assured no references to this {@code SEXP}
   * are being held elsewhere.
   *
   * @param attributeMap the new attribute map
   */
  public void unsafeSetAttributes(AttributeMap attributeMap) {
    this.attributes = attributeMap;
    this.object = attributes.hasClass();
  }
  
  public void unsafeSetAttributes(AttributeMap.Builder attributes) {
    unsafeSetAttributes(attributes.validateAndBuildFor(this));
  }

}
