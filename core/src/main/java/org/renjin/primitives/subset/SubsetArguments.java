package org.renjin.primitives.subset;

import com.google.common.collect.Lists;
import org.renjin.eval.EvalException;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.List;

/**
 * Arguments to the subscript operator
 */
public class SubsetArguments {
  
  private Vector source;
  private List<SEXP> subscripts = Lists.newArrayList();
  private SEXP replacement;
  
  private SubsetArguments() {}
  
  private void setSource(SEXP source) {
    if(source instanceof PairList.Node) {
      this.source = ((PairList.Node) source).toVector();
    } else if(source instanceof Vector) {
      this.source = (Vector) source;
    } else {
      throw new EvalException("Invalid source: " + source);
    }
  }
  
  public static SubsetArguments parseReplacementArguments(SEXP source, ListVector argumentList) {
    SubsetArguments args = new SubsetArguments();
    args.setSource(source);
    args.replacement = argumentList.getElementAsSEXP(argumentList.length() - 1);
    for(int i=0;i<argumentList.length()-1;++i) {
      args.subscripts.add(argumentList.get(i));
    }
    return args;
  }
  
  public boolean isListSource() {
    return source instanceof ListVector;
  }
  
  public ListVector getListSource() {
    return (ListVector) source;
  }

  public Vector getSource() {
    return source;
  }

  public List<SEXP> getSubscripts() {
    return subscripts;
  }

  public SEXP getReplacement() {
    return replacement;
  }
  
  public Selection parseSelection() {

    if(subscripts.isEmpty()) {
      return new CompleteSelection(source);

    } else if(subscripts.size() == 1) {

      SEXP subscript = subscripts.get(0);

      // if the single argument is a matrix or greater, then
      // we treat it as a matrix of coordinates
      if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {

        return new CoordinateMatrixSelection(source, subscript);

      } else {

        // otherwise we treat the source
        // as a vector, regardless of whether it has dimensions or not

        return new VectorIndexSelection(source, subscript);
      }

    } else {

      // otherwise we have multiple subscripts, and we treat each subscript
      // as applying as whole to its dimensions (including whole rows or columns
      // in the case of matrices)

      return new DimensionSelection(source, subscripts);

    }
  }
}
