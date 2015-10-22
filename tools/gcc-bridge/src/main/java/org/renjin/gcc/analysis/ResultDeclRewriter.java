package org.renjin.gcc.analysis;

import com.google.common.base.Predicates;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
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
    boolean returnDecl = fn.lhsMatches(Predicates.instanceOf(GimpleResultDecl.class));
    
    if(returnDecl) {
      // if so, replace it with a local variable
      GimpleVarDecl returnVar = fn.addVarDecl(fn.getReturnType());
      GimpleVariableRef ref = new GimpleVariableRef(returnVar.getId());
      
      fn.replaceAll(Predicates.instanceOf(GimpleResultDecl.class), ref);
      return true;

    } else {
      return false;
    }
  }
}
