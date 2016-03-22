package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;


public class NamedSelection implements Selection2 {
  
  private StringVector selectedNames;

  public NamedSelection(StringVector selectedNames) {
    this.selectedNames = selectedNames;
  }
  
  @Override
  public Vector replaceListElements(ListVector list, Vector replacement) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector replaceElements(AtomicVector source, Vector replacements) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListVector replaceSingleListElement(ListVector source, SEXP replacement) {

    String selectedName = computeUniqueName();
    
    // Try to match the name to the list's own names
    int selectedIndex = source.indexOfName(selectedName);
    boolean matched = (selectedIndex != -1);

    // In the context of the [[<- operator, assign NULL has the effect
    // of deleting an element
    boolean deleting = replacement == Null.INSTANCE;
    
    // If we are deleting, and there is no matching name, then
    // there is no need to copy
    if(deleting && !matched) {
      return source;
    }
    
    // Otherwise make a copy
    ListVector.NamedBuilder result = source.newCopyNamedBuilder();
    boolean deformed = false;
    
    if(deleting) {
      result.remove(selectedIndex);
      deformed = true;
    
    } else if(matched) {
      result.set(selectedIndex, replacement);
    
    } else {
      result.add(selectedName, replacement);
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
  public SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacements) {
    if(replacements.length() != 1) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    String selectedName = computeUniqueName();

    // Try to match the name to the list's own names
    int selectedIndex = source.getIndexByName(selectedName);
    boolean matched = (selectedIndex != -1);

    // Make a copy, promoting the result type if replacements is wider
    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());
    NamesBuilder resultNames = NamesBuilder.clonedFrom(source);
    boolean deformed = false;

    if(matched) {
      result.setFrom(selectedIndex, replacements, 0);
    } else {
      result.addFrom(replacements, 0);
      resultNames.add(selectedName);
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
//  
//  private Map<String, Integer> buildNameMap(AtomicVector sourceNames, AtomicVector selectedNames) {
//    Map<String, Integer> map = new HashMap<>();
//    
//  }
  
  
  private String computeUniqueName() {
    Selections.checkUnitLength(selectedNames);
    
    return selectedNames.getElementAsString(0);
  }
  
}
