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

package org.renjin.sexp;

abstract class AbstractVector extends AbstractSEXP implements Vector {

  protected AbstractVector(SEXP tag, AttributeMap attributes) {
    super(attributes);
  }

  protected AbstractVector(AttributeMap attributes) {
    super(attributes);
  }

  protected AbstractVector() {
  }

  @Override
  public boolean isElementTrue(int index) {
    return getElementAsRawLogical(index) == 1;
  }


  @Override
  public boolean isElementNaN(int index) {
    return isElementNA(index);
  }


  @Override
  public byte getElementAsByte(int index) {
    int value = getElementAsInt(index);
    if(value < 0 || value > 255) {
      return 0;
    } else {
      return (byte)value;
    }
  }

  @Override
  public Builder newCopyBuilder(Type replacementType) {
    if(getVectorType().isWiderThanOrEqualTo(replacementType)) {
      return newCopyBuilder();
    } else {
      Builder result;
      result = replacementType.newBuilderWithInitialSize(length());
      result.copyAttributesFrom(this);
      for(int i=0;i!=length();++i) {
        result.setFrom(i, this, i);
      }
      return result;
    }
  }

  public int getComputationDepth() {
    return 0;
  }

  abstract static class AbstractBuilder<S extends SEXP> implements Builder<S> {
    private final AttributeMap.Builder attributes = AttributeMap.builder();

    @Override
    public Builder setAttribute(String name, SEXP value) {
      return setAttribute(Symbol.get(name), value);
    }

    @Override
    public Builder setAttribute(Symbol name, SEXP value) {
      if(value != Null.INSTANCE) {
        attributes.set(name, value);
      }
      return this;
    }

    @Override
    public Builder removeAttribute(Symbol name) {
      attributes.remove(name);
      return this;
    }

    @Override
    public Builder setDim(int row, int col) {
      attributes.setDim(row, col);
      return this;
    }

    @Override
    public SEXP getAttribute(Symbol name) {
      return attributes.get(name);
    }

    @Override
    public Builder copyAttributesFrom(SEXP exp) {
      attributes.addAllFrom(exp.getAttributes());
      return this;
    }
    
    /**
     * Copies "special" attributes: 
     * @param exp
     * @return
     */
    @Override
    public Builder copySomeAttributesFrom(SEXP exp, Symbol... toCopy) {
      for(int i=0;i!=toCopy.length;++i) {
        attributes.addIfNotNull(exp.getAttributes(), toCopy[i]);
      }
      return this;
    }
    

    @Override
    public Builder addNA() {
      return setNA(length());
    }

    @Override
    public Builder addFrom(S source, int sourceIndex) {
      return setFrom(length(), source, sourceIndex);
    }

    protected AttributeMap buildAttributes() {
      return attributes.build();
    }
  }
}
