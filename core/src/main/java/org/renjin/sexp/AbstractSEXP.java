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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.renjin.eval.EvalException;

import java.util.Collections;


/**
 * Base class for R data types.
 */
public abstract class AbstractSEXP implements SEXP {

  protected PairList attributes;

  private final boolean object;

  protected AbstractSEXP() {
    this.attributes = Null.INSTANCE;
    this.object = false;
  }

  protected AbstractSEXP(PairList attributes) {
    Preconditions.checkNotNull(attributes);
    this.attributes = attributes;
    this.object = attributes.findByTag(Symbols.CLASS).length() > 0;
  }

  protected boolean checkDims() {
    Vector dimVector = (Vector)attributes.findByTag(Symbols.DIM);
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
    return attributes != null && attributes.length() != 0;
  }

  @Override
  public PairList getAttributes() {
    if(attributes == Null.INSTANCE) {
      return attributes;
    } else {
      return Attributes.expandAttributes((PairList)attributes);
    }
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
    SEXP classAttribute = attributes.findByTag(Symbols.CLASS);
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
    // either Null.INSTANCE or StringVector, enforced below
    return (AtomicVector) attributes.findByTag(Symbols.NAMES);
  }

  @Override
  public String getName(int index) {
    SEXP names = attributes.findByTag(Symbols.NAMES);
    if(names instanceof StringVector) {
      return ((StringVector) names).getElementAsString(index);
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
      SEXP namesExp = attributes.findByTag(Symbols.NAMES);
      if(namesExp instanceof StringVector) {
        StringVector names = (StringVector) namesExp;
        for(int i=0;i!=names.length();++i) {
          if(names.getElementAsString(i).equals(name)) {
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
    if(hasAttributes()) {
      return attributes.findByTag(name);
    }
    return Null.INSTANCE;
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
  public SEXP setAttributes(ListVector attributes) {
    PairList.Builder list = new PairList.Builder();
    for(int i=0;i!=attributes.length();++i) {
      String attributeName = attributes.getName(i);
      if(Strings.isNullOrEmpty(attributeName)) {
        throw new EvalException("Attributes must be named");
      }
      SEXP attributeValue = attributes.getElementAsSEXP(i);
      if(attributeValue != Null.INSTANCE) {
        list.add(Symbol.get(attributeName), 
            Attributes.validateAttribute(this, Symbol.get(attributeName), attributeValue));
      }
    }
    return cloneWithNewAttributes(list.build());
  }

  private PairList replaceAttribute(Symbol attributeName, SEXP newValue) {
    PairList.Node.Builder builder = new PairList.Builder();
    boolean replaced = false;
    for(PairList.Node node : attributes.nodes()) {
      if(node.getTag() == attributeName) {
        if(newValue != Null.INSTANCE) {
          builder.add(node.getTag(), newValue);
        }
        replaced = true;
      } else {
        builder.add(node.getRawTag(), node.getValue());
      }
    }
    if(!replaced && newValue != Null.INSTANCE) {
      builder.add(attributeName, newValue);
    }
    return builder.build();
  }

  protected SEXP cloneWithNewAttributes(PairList attributes) {
    throw new UnsupportedOperationException("cannot change/set attributes on " + getClass().getSimpleName());
  }

  @Override
  public Iterable<SEXP> elements() {
    return Collections.<SEXP>singleton(this);
  }

  @Override
  public <S extends SEXP> S getElementAsSEXP(int index) {
    if(index == 0) {
      return (S)this;
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  public SEXP force() {
    return this;
  }
  
}
