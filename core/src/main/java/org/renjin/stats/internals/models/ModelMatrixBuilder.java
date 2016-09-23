/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.stats.internals.models;

import org.renjin.eval.Context;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;
import org.renjin.stats.internals.models.TermsObject.Term;

import java.util.List;

/**
 * Constructs a model matrix from a {@link TermsObject} and a
 * {@link ModelFrame}
 */
public class ModelMatrixBuilder {

  public static ModelMatrix build(Context context, SEXP termsObject, ListVector frame) {
    ModelFrame modelFrame = new ModelFrame(frame);
    TermsObject terms = new TermsObject(termsObject, modelFrame);

    // make a list of all of the columns of the terms, and keep
    // track of the terms to which they belong with the 'assign'
    // attribute
    List<ModelMatrixColumn> columns = Lists.newArrayList();
    IntArrayVector.Builder assignment = new IntArrayVector.Builder();
    
    if (terms.hasIntercept()) {
      columns.add(new InterceptColumn());
      assignment.add(0);
    }
    for(Term term : terms.getTerms()) {
      if(!term.isResponse()) {
        for(ModelMatrixColumn column : term.getModelMatrixColumns()) {
          columns.add(column);
          assignment.add(term.getTermIndex() + 1);
        }
      }
    }
        
    // now compose the attributes that describe the matrix
    AttributeMap.Builder attributes = AttributeMap.builder();
    attributes.set(Symbols.DIM, new IntArrayVector(modelFrame.getNumRows(), columns.size()));
    attributes.set(Symbols.DIMNAMES, new ListVector(modelFrame.getRowNames(), columnNames(columns)));
    attributes.set(ModelMatrix.ASSIGN, assignment.build());
    // TODO: contrasts

    return new ModelMatrix(modelFrame.getNumRows(), columns, attributes.build());
  }
  
  private static StringVector columnNames(List<ModelMatrixColumn> columns) {
    StringVector.Builder names = StringVector.newBuilder();
    for(ModelMatrixColumn column : columns) {
      names.add(column.getName());
    }
    return names.build();
  }
}
