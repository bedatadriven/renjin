package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

import java.util.HashMap;
import java.util.Map;


public class NamedSelection implements Selection2 {
  
  private StringVector selectedNames;

  public NamedSelection(StringVector selectedNames) {
    this.selectedNames = selectedNames;
  }
  
  @Override
  public Vector replaceListElements(ListVector source, Vector replacement) {
    return buildReplacement(source, replacement);
  }

  @Override
  public Vector replaceElements(AtomicVector source, Vector replacements) {
    return buildReplacement(source, replacements);
  }
  
  private Vector buildReplacement(Vector source, Vector replacements) {

    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());
    NamesBuilder resultNames = NamesBuilder.clonedFrom(source);

    Map<String, Integer> nameMap = buildNameMap(source);

    int replacementIndex = 0;
    
    for (int i = 0; i < selectedNames.length(); i++) {
      String selectedName = selectedNames.getElementAsString(i);
      Integer index = nameMap.get(selectedName);
      
      if(index != null) {
        result.setFrom(index, replacements, replacementIndex++);

      } else {
        int newIndex = result.length();
        result.setFrom(newIndex, replacements, replacementIndex++);
        resultNames.add(selectedName);
        nameMap.put(selectedName, newIndex);
      }
      
      if(replacementIndex >= replacements.length()) {
        replacementIndex = 0;
      }
    }
    
    if(replacementIndex != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    result.setAttribute(Symbols.NAMES, resultNames.build());
    
    return result.build();
  }

  private Map<String, Integer> buildNameMap(Vector source) {
    
    Map<String, Integer> map = new HashMap<>();
    
    if(source.getAttributes().hasNames()) {
      StringVector names = source.getAttributes().getNames();
      for (int i = 0; i < names.length(); i++) {
        String name = names.getElementAsString(i);
        if(!map.containsKey(name)) {
          map.put(name, i);
        }
      }
    }
    
    return map;
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

  @Override
  public SEXP get(Vector source, boolean drop) {
    throw new UnsupportedOperationException();
  }

  private String computeUniqueName() {
    Selections.checkUnitLength(selectedNames);
    
    return selectedNames.getElementAsString(0);
  }
  
}
