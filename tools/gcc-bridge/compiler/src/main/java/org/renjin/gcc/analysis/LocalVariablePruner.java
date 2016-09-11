package org.renjin.gcc.analysis;

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.ListIterator;
import java.util.Set;

/**
 * Removes unused variables. 
 * 
 * <p>These can include arrays which are actually dynamically allocated and not used.</p>
 */
public class LocalVariablePruner implements FunctionBodyTransformer {
  
  public static final LocalVariablePruner INSTANCE = new LocalVariablePruner();
  
  @Override
  public boolean transform(TreeLogger logger, GimpleCompilationUnit unit, GimpleFunction fn) {

    VariableRefFinder refFinder = new VariableRefFinder();
    fn.accept(refFinder);
    
    ListIterator<GimpleVarDecl> it = fn.getVariableDeclarations().listIterator();
    boolean updated = false;
    while(it.hasNext()) {
      GimpleVarDecl decl = it.next();
      if(!refFinder.used.contains(decl.getId())) {
        it.remove();
        updated = true;
      }
    }
    return updated;
  }
  
  private class VariableRefFinder extends GimpleExprVisitor {

    private Set<Integer> used = Sets.newHashSet();
    
    @Override
    public void visitVariableRef(GimpleVariableRef variableRef) {
      used.add(variableRef.getId());
    }
  }
}
