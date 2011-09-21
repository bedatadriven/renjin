package r.jvmi.wrapper;

import r.lang.Context;
import r.lang.DotExp;
import r.lang.Environment;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;

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
  
    SEXP arg = node.getValue();
    
    if(Symbol.ELLIPSES.equals(arg)) {
      DotExp dotdot = (DotExp) arg.evalToExp(context, rho);
      elipses = dotdot.getPromises();
      return next();

    } else {
     
      return arg;
    } 
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
    
    if(Symbol.ELLIPSES.equals(arg)) {
      DotExp dotdot = (DotExp) arg.evalToExp(context, rho);
      elipses = dotdot.getPromises();
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
      if(Symbol.ELLIPSES.equals(arg)) {
        DotExp dotdot = (DotExp) arg.evalToExp(context, rho);
        elipses = dotdot.getPromises();
        args = ((PairList.Node)args).getNext();

        return hasNext();
        
      } else {
        return true;
      }
    }
    
    return false;
  }
  

}
