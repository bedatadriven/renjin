/**
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
  public boolean isDeferred() {
    return false;
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
  public double getElementAsComplexIm(int index) {
    return 0d;
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

  @Override
  public void copyTo(double[] array, int offset, int length) {
    for (int i = 0; i < length; i++) {
      array[offset + i] = getElementAsDouble(i);
    }
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

    @Override
    public Builder combineAttributesFrom(SEXP vector) {
      AttributeMap attributes = vector.getAttributes();
      if(attributes != AttributeMap.EMPTY) {
        this.attributes.combineFrom(attributes);
      }
      return this;
    }

    @Override
    public Builder combineStructuralAttributesFrom(SEXP vector) {
      AttributeMap vectorAttributes = vector.getAttributes();
      if(vectorAttributes != AttributeMap.EMPTY) {
        this.attributes.combineStructuralFrom(vectorAttributes);
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
