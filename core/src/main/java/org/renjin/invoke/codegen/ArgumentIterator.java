package org.renjin.invoke.codegen;

import org.renjin.eval.Context;
import org.renjin.sexp.*;


/**
 * Iterates over an argument list, nesting into ... as necessary.
 * 
 * <p>For example:
 * <p>
 * <code>
 * f&lt;-function(...) pow(..., 2)
 * f(8)
 * </code>
 * 
 * <p>When pow(...,2) is reached, the symbol '...` (Symbol.ELIPSES) must be evaluated
 * and merged into the list of arguments. 
 * 
 */
public class ArgumentIterator {

  private Context context;
  private Environment rho;
  private PairList args;
  private PairList ellipses = Null.INSTANCE;
  private String currentName;

  public ArgumentIterator(Context context, Environment rho, PairList args) {
    super();
    this.context = context;
    this.rho = rho;
    this.args = args;
  }
  
  public SEXP evalNext() {  
    PairList.Node node;
    if(ellipses != Null.INSTANCE) {
      node = ((PairList.Node) ellipses);
      ellipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException("too few arguments");
    }
  
    SEXP value = node.getValue();


    if(Symbols.ELLIPSES.equals(value)) {
      PromisePairList dotdot = (PromisePairList) context.evaluate( value, rho);
      ellipses = dotdot;
      return evalNext();

    } else {
      this.currentName = node.getName();
      SEXP evaluated = context.evaluate(value, rho);
//      if(evaluated == Symbol.MISSING_ARG) {
//        throw new EvalException("Missing argument with no default: " + value);
//      }
      return evaluated;
    } 
  }
  
  public SEXP next() {  
    PairList.Node node;
    if(ellipses != Null.INSTANCE) {
      node = ((PairList.Node) ellipses);
      ellipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException("too few arguments");
    }

    this.currentName = node.getName();
    return node.getValue(); 
  }
  
  public PairList.Node nextNode() {  
    PairList.Node node;
    if(ellipses != Null.INSTANCE) {
      node = ((PairList.Node) ellipses);
      ellipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException("too few arguments");
    }
  
    SEXP arg = node.getValue();
    
    if(Symbols.ELLIPSES.equals(arg)) {
      PromisePairList dotdot = (PromisePairList) context.evaluate(arg, rho);
      ellipses = dotdot;
      return nextNode();

    } else {
     
      return node;
    } 
  }

  
  public boolean hasNext() {
    if(ellipses != Null.INSTANCE) {
      return true;
    }
    
    if(args != Null.INSTANCE) {
      SEXP arg = ((PairList.Node)args).getValue();
      if(Symbols.ELLIPSES.equals(arg)) {
        PromisePairList dotdot = (PromisePairList) context.evaluate(arg, rho);
        ellipses = dotdot;
        args = ((PairList.Node)args).getNext();

        return hasNext();
        
      } else {
        return true;
      }
    }
    
    return false;
  }
}
