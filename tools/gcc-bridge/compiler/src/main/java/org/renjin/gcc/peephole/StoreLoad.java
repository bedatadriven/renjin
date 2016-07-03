package org.renjin.gcc.peephole;

import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.tree.AbstractInsnNode;
import org.renjin.repackaged.asm.tree.IincInsnNode;
import org.renjin.repackaged.asm.tree.InsnList;
import org.renjin.repackaged.asm.tree.VarInsnNode;

import java.util.ListIterator;


/**
 * Removes sequential store/load instructions to the same local variable.
 */
public class StoreLoad implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    
    if(it.matches(Pattern.STORE, Pattern.LOAD)) {
      VarInsnNode store = it.get(0);
      VarInsnNode load = it.get(1);
      
      if(store.var == load.var) {
        // We can only remove the STORE/LOAD sequence if there is exactly ONE load
        // of this variable in the whole function
        if(countLoads(it.getList(), store.var) == 1) {
          it.remove(2);
          return true; 
        } else {
          // if we can't completely remove the instruction pair, then at least eliminate the load 
          // instruction by preceding the 
          
        }
      }
    }
    return false;
  }

  private int countLoads(InsnList list, int var) {
    int count = 0;
    ListIterator<AbstractInsnNode> it = list.iterator();
    while(it.hasNext()) {
      AbstractInsnNode node = it.next();
      if (Pattern.LOAD.match(node)) {
        VarInsnNode load = (VarInsnNode) node;
        if(load.var == var) {
          count++;
        }
      } else if(node.getOpcode() == Opcodes.IINC) {
        IincInsnNode inc = (IincInsnNode) node;
        if(inc.var == var) {
          count++;
        }
      }
    }
    return count;
  }
}
