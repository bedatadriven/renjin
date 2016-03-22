package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

/**
 * Simple selection using postive or negative indexes
 */
public class IndexSelection implements Selection2 {

  private final AtomicVector subscript;

  public IndexSelection(AtomicVector subscript) {
    this.subscript = subscript;
  }

  @Override
  public SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public ListVector replaceSingleListElement(ListVector source, SEXP replacement) {

    int selectedIndex = computeUniqueIndex(source);

    // In the context of the [[<- operator, assign NULL has the effect
    // of deleting an element
    boolean deleting = replacement == Null.INSTANCE;
    boolean exists = (selectedIndex < source.length());

    // If we are deleting, and there is no nth element, 
    // we can just return a copy
    if(deleting && !exists) {
      return source;
    }

    // Otherwise make a copy
    ListVector.NamedBuilder result = source.newCopyNamedBuilder();
    boolean deformed = false;

    if(deleting) {
      result.remove(selectedIndex);
      deformed = true;

    } else if(exists) {
      result.set(selectedIndex, replacement);

    } else {
      result.set(selectedIndex, replacement);
      deformed = true;
    }

    // If we've changed the shape of the list, we need to drop matrix-related
    // attributes which are no longer valid
    if(deformed) {
      result.removeAttribute(Symbols.DIM);
      result.removeAttribute(Symbols.DIMNAMES);
    }

    return result.build();
  }

  private int computeUniqueIndex(Vector source) {
    return new IndexSubscript(subscript, source.length()).computeUniqueIndex();
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
    
    if(replacement.length() == 0) {
      throw new EvalException("replacement has length zero");
    }
    
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector replaceListElements(ListVector source, Vector replacements) {
    IndexSubscript subscript = new IndexSubscript(this.subscript, source.length());


    // When replace items on a list, list[i] <- NULL has the meaning of 
    // removing all selected elements
    if(replacements == Null.INSTANCE) {
      return ListSubsetting.removeListElements(source, subscript.computeIndexPredicate());
    }
    
    // Otherwise update or expand items using the subscripts
    ListVector.NamedBuilder result = source.newCopyNamedBuilder();
    IndexIterator2 it = subscript.indexIterator();
    
    boolean deformed = false;
    
    int index;
    int replacementIndex = 0;
    while((index=it.next()) != IndexIterator2.EOF) {
      
      result.setFrom(index, replacements, replacementIndex++);
      
      if(replacementIndex >= replacements.length()) {
        replacementIndex = 0;
      }
    }

    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector replaceElements(AtomicVector source, Vector replacements) {

    return buildReplacement(source, replacements,  new IndexSubscript(this.subscript, source.length()));
  }

  
  private Vector buildReplacement(AtomicVector source, Vector replacements, IndexSubscript subscript) {
    
    Vector.Builder builder = source.newCopyBuilder(replacements.getVectorType());
    
    boolean deformed = false;
    
    int replacementIndex = 0;

    int index;
    IndexIterator2 it = subscript.indexIterator();
    while((index=it.next()) != IndexIterator2.EOF) {
      if(!IntVector.isNA(index)) {

        if(index >= source.length()) {
          deformed = true;
        }
        
        builder.setFrom(index, replacements, replacementIndex++);

        if (replacementIndex >= replacements.length()) {
          replacementIndex = 0;
        }
      }
    }
    
    if(deformed) {
      builder.removeAttribute(Symbols.DIM);
      builder.removeAttribute(Symbols.DIMNAMES);
    }

    return builder.build();
  }

}
