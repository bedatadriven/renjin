package org.renjin.primitives.subset;

import com.google.common.collect.Maps;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

import java.util.HashMap;
import java.util.Map;


/**
 * Selects elements from a {@code source} expression by name.
 *
 */
class NamedSelection implements SelectionStrategy {
  
  private static final int NOT_FOUND = -1;
  
  private static final int MULTIPLE_PARTIAL_MATCHES = -2;
  
  private StringVector selectedNames;

  public NamedSelection(StringVector selectedNames) {
    this.selectedNames = selectedNames;
  }

  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {

    Map<String, Integer> nameMap = buildNameMap(source);
    
    Vector.Builder result = source.newBuilderWithInitialCapacity(selectedNames.length());

    for (int i = 0; i < selectedNames.length(); i++) {
      String selectedName = selectedNames.getElementAsString(i);
      Integer index = nameMap.get(selectedName);
      if(index == null) {
        result.addNA();        
      } else {
        result.addFrom(source, index);
      }
    }
    
    result.setAttribute(Symbols.NAMES, selectedNames.setAttributes(AttributeMap.EMPTY));

    return result.build();
  }

  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {

    // First check that we have at least one name
    if(selectedNames.length() == 0) {
      throw new EvalException("attempt to select less than one element from a lang object");
    }

    // Build a map from name to linked list node
    Map<String, SEXP> nameMap = Maps.newHashMap();
    for (PairList.Node node : call.nodes()) {
      if(node.hasName()) {
        nameMap.put(node.getName(), node.getValue());
      }
    }

    FunctionCall.Builder newCall = FunctionCall.newBuilder();


    // Now iterator look up the names
    // Starting with the function,
    for (String selectedName : selectedNames) {
      SEXP value = nameMap.get(selectedName);
      if (value == null) {
        newCall.add(selectedName, Null.INSTANCE);
      } else {
        newCall.add(selectedName, value);
      }
    }

    return newCall.build();
  }


  @Override
  public Vector replaceListElements(Context context, ListVector source, Vector replacements) {
    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());
    NamesBuilder resultNames = NamesBuilder.clonedFrom(source);

    Map<String, Integer> nameMap = buildNameMap(source);

    int replacementIndex = 0;
    int replacementLength = replacements.length();

    for (int i = 0; i < selectedNames.length(); i++) {
      String selectedName = selectedNames.getElementAsString(i);
      Integer index = nameMap.get(selectedName);

      if(index != null) {
        result.setFrom(index, replacements, replacementIndex++);

      } else {
        int newIndex = result.length();
        if(replacements == Null.INSTANCE) {
          // NULL is special case ONLY for replacement list elements,
          // not atomic vectors
          // do nothing
        } else if(replacements.length() == 0) {
          throw new EvalException("replacement has zero length");
          
        } else {
          result.setFrom(newIndex, replacements, replacementIndex++);
          resultNames.add(selectedName);
          nameMap.put(selectedName, newIndex);
        }
      }

      if(replacementIndex >= replacementLength) {
        replacementIndex = 0;
      }
    }

    if(replacementIndex != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    result.setAttribute(Symbols.NAMES, resultNames.build());

    // Drop dim attributes
    result.removeAttribute(Symbols.DIM);
    result.removeAttribute(Symbols.DIMNAMES);
    
    return result.build();
  }

  @Override
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {
    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());
    NamesBuilder resultNames = NamesBuilder.clonedFrom(source);

    Map<String, Integer> nameMap = buildNameMap(source);

    int replacementIndex = 0;
    int replacementLength = replacements.length();

    for (int i = 0; i < selectedNames.length(); i++) {
      String selectedName = selectedNames.getElementAsString(i);
      Integer index = nameMap.get(selectedName);

      if(index != null) {
        result.setFrom(index, replacements, replacementIndex++);

      } else {
        if(replacementLength == 0) {
          throw new EvalException("replacement has zero length");
        }
        int newIndex = result.length();
        result.setFrom(newIndex, replacements, replacementIndex++);
        resultNames.add(selectedName);
        nameMap.put(selectedName, newIndex);
      }

      if(replacementIndex >= replacementLength) {
        replacementIndex = 0;
      }
    }

    if(replacementIndex != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    result.setAttribute(Symbols.NAMES, resultNames.build());

    // Drop dim attributes
    result.removeAttribute(Symbols.DIM);
    result.removeAttribute(Symbols.DIMNAMES);

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
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    int index = findSelectedElement(source, exact);
    
    if(index == NOT_FOUND) {
      return Null.INSTANCE;
    }
    
    return source.getElementAsSEXP(index);
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {
    int index = findSelectedElement(source, exact);

    // Note that behavior is different here than lists. If there is no
    // match, throw an exception rather than return NULL
    if(index == NOT_FOUND) {
      throw SubsetAssertions.outOfBounds();
    }

    return source.getElementAsSEXP(index);
  }
  
  private int findSelectedElement(Vector source, boolean exact) {

    String selectedName = computeUniqueSelectedName();
    
    if (!source.getAttributes().hasNames()) {
      return NOT_FOUND;
    }
    StringVector names = source.getAttributes().getNames();

    int partialMatch = NOT_FOUND;
    
    for(int i=0;i!=names.length();++i) {
      String name = names.getElementAsString(i);
      if(StringVector.isNA(name) && StringVector.isNA(selectedName)) {
        return i;
      }
      // Exact matches always win
      if(name.equals(selectedName)) {
        return i;
      }
      // Check for partial match
      if(!exact) {
        if(name.startsWith(selectedName)) {
          // if this is the first partial match, remember it 
          // but keep looking for other partial matches.
          // We only accept partial matches if there is exactly ONE partial match
          if(partialMatch == NOT_FOUND) {
            partialMatch = i;
          } else {
            // This is the second partial match we've found, so 
            // we won't accept it. However, we do need to keep looking for partial matches.
            partialMatch = MULTIPLE_PARTIAL_MATCHES;
          }
        }
      }
    }
    
    if(partialMatch >= 0) {
      return partialMatch;
    }

    return NOT_FOUND;
  }

  @Override
  public ListVector replaceSingleListElement(ListVector source, SEXP replacement) {

    String selectedName = computeUniqueSelectedName();
    
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
  public SEXP replaceSinglePairListElement(PairList.Node source, SEXP replacement) {
    
    String selectedName = computeUniqueSelectedName();
    boolean found = false;

    PairList.Builder newList = new PairList.Builder();
    for (PairList.Node node : source.nodes()) {
      if(!found && node.hasTag() && node.getTag().getPrintName().equals(selectedName)) {
        if(replacement != Null.INSTANCE) {
          newList.add(node.getTag(), replacement);
        }
        found = true;
      } else {
        newList.add(node.getRawTag(), node.getValue());
      }
    }
    if(!found && replacement != Null.INSTANCE) {
      newList.add(selectedName, replacement);
    }
  
    return newList.build();
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacements) {
    if(replacements.length() != 1) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    String selectedName = computeUniqueSelectedName();

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
      result.setAttribute(Symbols.NAMES, resultNames.build());
      result.removeAttribute(Symbols.DIM);
      result.removeAttribute(Symbols.DIMNAMES);
    }
    return result.build();
  }

  private String computeUniqueSelectedName() {
    SubsetAssertions.checkUnitLength(selectedNames);
    
    return selectedNames.getElementAsString(0);
  }
}
