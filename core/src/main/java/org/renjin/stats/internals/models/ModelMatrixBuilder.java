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
