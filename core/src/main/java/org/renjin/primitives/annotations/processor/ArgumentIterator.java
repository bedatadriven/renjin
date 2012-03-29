package org.renjin.primitives.annotations.processor;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.PromisePairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;


/**
 * Iterates over an argument list, nesting into ... as necessary.
 * 
 * <p>For example:
 * <p>
 * <code>
 * f&lt;-function(...) pow(..., 2)
 * f(8)
 * </cope>
 * 
 * <p>When pow(...,2) is reached, the symbol '...` (Symbol.ELIPSES) must be evaluated
 * and merged into the list of arguments. 
 * 
 */
public class ArgumentIterator {

  private Context context;
  private Environment rho;
  private PairList args;
  private PairList elipses = Null.INSTANCE;
  
  public ArgumentIterator(Context context, Environment rho, PairList args) {
    super();
    this.context = context;
    this.rho = rho;
    this.args = args;
  }
  
  public SEXP evalNext() {  
    PairList.Node node;
    if(elipses != Null.INSTANCE) {
      node = ((PairList.Node)elipses);
      elipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException();
    }
  
    SEXP arg = node.getValue();
    
    if(Symbols.ELLIPSES.equals(arg)) {
      PromisePairList dotdot = (PromisePairList) context.evaluate( arg, rho);
      elipses = dotdot;
      return evalNext();

    } else {
     
      return context.evaluate( arg, rho);
    } 
  }
  
  public SEXP next() {  
    PairList.Node node;
    if(elipses != Null.INSTANCE) {
      node = ((PairList.Node)elipses);
      elipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException();
    }
  
    return node.getValue(); 
  }
  
  public PairList.Node nextNode() {  
    PairList.Node node;
    if(elipses != Null.INSTANCE) {
      node = ((PairList.Node)elipses);
      elipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException();
    }
  
    SEXP arg = node.getValue();
    
    if(Symbols.ELLIPSES.equals(arg)) {
      PromisePairList dotdot = (PromisePairList) context.evaluate(arg, rho);
      elipses = dotdot;
      return nextNode();

    } else {
     
      return node;
    } 
  }
  
  public boolean hasNext() {
    if(elipses != Null.INSTANCE) {
      return true;
    }
    
    if(args != Null.INSTANCE) {
      SEXP arg = ((PairList.Node)args).getValue();
      if(Symbols.ELLIPSES.equals(arg)) {
        PromisePairList dotdot = (PromisePairList) context.evaluate(arg, rho);
        elipses = dotdot;
        args = ((PairList.Node)args).getNext();

        return hasNext();
        
      } else {
        return true;
      }
    }
    
    return false;
  }
  

}
