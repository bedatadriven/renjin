package org.renjin.gcc.analysis;

import com.google.common.base.Predicates;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleResultDecl;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

/**
 * Rewrites {@code GimpleResultDecl} nodes into local variables.
 * 
 * <p>The {@code result_decl} lvalue is some weird fortran thing. Much easier to deal with 
 * as a simple local variable.</p>
 */
public class ResultDeclRewriter implements FunctionBodyTransformer {

  public static final ResultDeclRewriter INSTANCE = new ResultDeclRewriter();

  @Override
  public boolean transform(GimpleCompilationUnit unit, GimpleFunction fn) {
    
    // Does this function use a return_decl expression?
    ResultDeclMatcher matcher = new ResultDeclMatcher();
    fn.accept(matcher);
    
    if(matcher.present) {
      // if so, replace it with a local variable
      GimpleVarDecl returnVar = fn.addVarDecl(fn.getReturnType());
      GimpleVariableRef ref = new GimpleVariableRef(returnVar.getId(), fn.getReturnType());
      
      fn.replaceAll(Predicates.instanceOf(GimpleResultDecl.class), ref);
      return true;

    } else {
      return false;
    }
  }
  
  private class ResultDeclMatcher extends GimpleExprVisitor {
    
    private boolean present = false;
    
    @Override
    public void visitResultDecl(GimpleResultDecl resultDecl) {
      present = true;
    }
  }
  
}
