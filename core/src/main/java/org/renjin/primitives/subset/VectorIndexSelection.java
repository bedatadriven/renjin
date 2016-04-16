package org.renjin.primitives.subset;

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.List;

import static org.renjin.primitives.subset.SubsetAssertions.checkBounds;

/**
 * Simple selection using positive or negative indexes of elements in a vector. Any {@code dim} attributes
 * are ignored.
 */
class VectorIndexSelection implements SelectionStrategy {

  private final AtomicVector subscript;

  public VectorIndexSelection(AtomicVector subscript) {
    this.subscript = subscript;
  }

  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {
    return buildSelection(source, new IndexSubscript(this.subscript, source.length()));
  }

  public static Vector buildSelection(Vector source, Subscript subscript) {
    
    IndexIterator it = subscript.computeIndexes();

    Vector.Builder result = source.getVectorType().newBuilder();
    AtomicVector sourceNames = source.getNames();
    StringArrayVector.Builder resultNames = null;
    if(sourceNames instanceof StringVector) {
      resultNames = new StringArrayVector.Builder();
    }

    int index;
    while((index=it.next())!= IndexIterator.EOF) {
      
      if(IntVector.isNA(index) || index >= source.length()) {
        result.addNA();
        if(resultNames != null) {
          resultNames.addNA();
        }

      } else {
        result.addFrom(source, index);
        if(resultNames != null) {
          resultNames.add(sourceNames.getElementAsString(index));
        }
      }
    }

    if(resultNames != null) {
      result.setAttribute(Symbols.NAMES, resultNames.build());
    }

    return result.build();
  }


  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {
    
    return buildCallSelection(call, new IndexSubscript(subscript, call.length()));
  }

  public static PairList buildCallSelection(FunctionCall call, Subscript subscript) {

    // First build an array from which we can lookup indices in normal time
    List<PairList.Node> nodes = Lists.newArrayList();
    for (PairList.Node node : call.nodes()) {
      nodes.add(node);
    }
    
    // Now construct a new function call by looking up the indexes
    FunctionCall.Builder newCall = FunctionCall.newBuilder();
    IndexIterator it = subscript.computeIndexes();
    int index;
    while((index=it.next())!= IndexIterator.EOF) {
      if(IntVector.isNA(index) || index >= nodes.size()) {
        newCall.add(nodes.get(index));
      } else {
        PairList.Node node = nodes.get(index);
        newCall.add(node.getRawTag(), node.getValue());
      }
    }
    
    return newCall.build();
  }
  
  @Override
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    IndexSubscript subscript = new IndexSubscript(this.subscript, source.length());

    // verify that we are selecting a single element
    int index = subscript.computeUniqueIndex();
    
    // Note that behavior of NA indices is different for lists than
    // atomic vectors below.
    if(IntVector.isNA(index)) {
      return Null.INSTANCE;
    }
    
    checkBounds(source, index);
    
    return source.getElementAsSEXP(index);
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {

    IndexSubscript subscript = new IndexSubscript(this.subscript, source.length());

    // verify that we are selecting a single element
    int index = subscript.computeUniqueIndex();
    
    // assert that the index is within bounds
    checkBounds(source, index);
    
    return source.getElementAsSEXP(index);
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
  public Vector replaceListElements(Context context, ListVector source, Vector replacements) {
    IndexSubscript subscript = new IndexSubscript(this.subscript, source.length());
    
    // When replace items on a list, list[i] <- NULL has the meaning of 
    // removing all selected elements
    if(replacements == Null.INSTANCE) {
      return ListSubsetting.removeListElements(source, subscript.computeIndexPredicate());
    }
    
    return buildReplacement(source, replacements, subscript);
  }

  @Override
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {
    return buildReplacement(source, replacements,  new IndexSubscript(this.subscript, source.length()));
  }

  
  public static Vector buildReplacement(Vector source, Vector replacements, Subscript subscript) {
    
    Vector.Builder builder = source.newCopyBuilder(replacements.getVectorType());
    StringVector.Builder resultNames = null;
    if(source.getAttributes().hasNames()) {
      resultNames = source.getAttributes().getNames().newCopyBuilder();
    }
    
    boolean deformed = false;
    
    int replacementIndex = 0;
    int replacementLength = replacements.length();

    int index;
    IndexIterator it = subscript.computeIndexes();
    while((index=it.next()) != IndexIterator.EOF) {
      
      if(index >= source.length()) {
        deformed = true;
        if(resultNames != null) {
          while(resultNames.length() <= index) {
            resultNames.add("");
          }
        }
      }
      
      if(replacementLength == 0) {
        throw new EvalException("replacement has zero length");
      }
      
      builder.setFrom(index, replacements, replacementIndex++);

      if (replacementIndex >= replacementLength) {
        replacementIndex = 0;
      }
    }
    
    if(deformed) {
      if(resultNames != null) {
        builder.setAttribute(Symbols.NAMES, resultNames.build());
      }
      builder.removeAttribute(Symbols.DIM);
      builder.removeAttribute(Symbols.DIMNAMES);
    }

    return builder.build();
  }

}
