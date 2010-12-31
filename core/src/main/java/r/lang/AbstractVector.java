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

package r.lang;

import com.google.common.collect.Maps;

import java.util.Map;

abstract class AbstractVector extends AbstractSEXP implements Vector {

  protected AbstractVector(SEXP tag, PairList attributes) {
    super(tag, attributes);
  }

  protected AbstractVector(PairList attributes) {
    super(attributes);
  }

  protected AbstractVector() {
  }

  abstract static class AbstractBuilder<S extends SEXP> implements Builder<S> {
    private final Map<SymbolExp,SEXP> attributes = Maps.newHashMap();

    @Override
    public Builder setAttribute(String name, SEXP value) {
      return setAttribute(new SymbolExp(name), value);
    }

    public Builder setAttribute(SymbolExp name, SEXP value) {
      attributes.put(name, value);
      return this;
    }

    public Builder copyAttributesFrom(SEXP exp) {
      for(PairList.Node node : exp.getAttributes().nodes()) {
        attributes.put(node.getTag(), node.getValue());
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

    protected PairList buildAttributes() {
      PairList.Node.Builder pairList = PairList.Node.newBuilder();
      for(Map.Entry<SymbolExp, SEXP> pair : attributes.entrySet()) {
        if(pair.getValue() != Null.INSTANCE) {
          pairList.add(pair.getKey(), pair.getValue());
        }
      }
      return pairList.build();
    }
  }



}
