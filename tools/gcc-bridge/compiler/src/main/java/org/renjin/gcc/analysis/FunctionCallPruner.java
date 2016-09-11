package org.renjin.gcc.analysis;

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.ListIterator;
import java.util.Set;

/**
 * Removes NOOP function calls
 */
public class FunctionCallPruner implements FunctionBodyTransformer {

  public static final FunctionCallPruner INSTANCE = new FunctionCallPruner();
  
  private Set<String> noops = Sets.newHashSet();
  
  public FunctionCallPruner() {
    noops.add("__builtin_stack_save");
    noops.add("__builtin_stack_restore");
  }

  @Override
  public boolean transform(TreeLogger logger, GimpleCompilationUnit unit, GimpleFunction fn) {
    
    boolean updated = false;
    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      ListIterator<GimpleStatement> it = basicBlock.getStatements().listIterator();
      while(it.hasNext()) {
        GimpleStatement statement = it.next();
        if(isNoop(statement)) {
          it.remove();
          updated = true;
        }
      }
    }
    
    return updated;
  }

  private boolean isNoop(GimpleStatement statement) {
    if(statement instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) statement;
      if (call.getFunction() instanceof GimpleAddressOf) {
        GimpleAddressOf addressOf = (GimpleAddressOf) call.getFunction();
        if(addressOf.getValue() instanceof GimpleFunctionRef) {
          GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
          return noops.contains(ref.getName());
        }
      }
    }
    return false;
  }
}
