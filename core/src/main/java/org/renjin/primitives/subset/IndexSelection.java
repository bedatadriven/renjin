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
    return replaceSingeListOrPairListElement(
        list.newCopyBuilder(), 
        replacement);
  }
  
  @Override
  public ListVector replaceSingleListElement(ListVector source, SEXP replacement) {

    return (ListVector) replaceSingeListOrPairListElement(
        source.newCopyNamedBuilder(), 
        replacement);
  }

  private SEXP replaceSingeListOrPairListElement(ListBuilder result, SEXP replacement) {
    
    // Find the index of the element to replace
    int selectedIndex = new IndexSubscript(subscript, result.length())
        .computeUniqueIndex();
    

    // In the context of the [[<- operator, assign NULL has the effect
    // of deleting an element
    boolean deleting = replacement == Null.INSTANCE;
    boolean exists = (selectedIndex < result.length());
    
    // Otherwise make a copy
    boolean deformed = false;

    if(deleting) {
      if(exists) {
        result.remove(selectedIndex);
        deformed = true;
      }
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


  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
    
    if(replacement.length() == 0) {
      throw new EvalException("replacement has length zero");
    }

    IndexSubscript subscript = new IndexSubscript(this.subscript, source.length());

    // verify that we are selecting a single element
    subscript.computeUniqueIndex();

    // Build the replacement
    return buildReplacement(source, replacement, subscript);
  }

  @Override
  public Vector replaceListElements(ListVector source, Vector replacements) {
    IndexSubscript subscript = new IndexSubscript(this.subscript, source.length());


    // When replace items on a list, list[i] <- NULL has the meaning of 
    // removing all selected elements
    if(replacements == Null.INSTANCE) {
      return ListSubsetting.removeListElements(source, subscript.computeIndexPredicate());
    }
    
    return buildReplacement(source, replacements, subscript);
  }
  
  @Override
  public Vector replaceElements(AtomicVector source, Vector replacements) {

    return buildReplacement(source, replacements,  new IndexSubscript(this.subscript, source.length()));
  }

  
  private Vector buildReplacement(Vector source, Vector replacements, IndexSubscript subscript) {
    
    Vector.Builder builder = source.newCopyBuilder(replacements.getVectorType());
    
    boolean deformed = false;
    
    int replacementIndex = 0;

    int index;
    IndexIterator2 it = subscript.computeIndexes();
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
