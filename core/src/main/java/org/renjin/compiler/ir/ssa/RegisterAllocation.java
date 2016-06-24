package org.renjin.compiler.ir.ssa;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.Map;

/**
 * Assigns our SSA variables to individual local variable
 * slots. Currently naively implemented, where each
 * variable gets its own slot.
 */
public class RegisterAllocation {

  private ControlFlowGraph cfg;
  private int nextSlot;
  private int size = 0;
  private Map<LValue, Integer> registers = Maps.newHashMap();

  public RegisterAllocation(ControlFlowGraph cfg, int nextSlot) {
    this.cfg = cfg;
    this.nextSlot = nextSlot;
    assign();
  }

  private void assign() {

    for(BasicBlock bb : cfg.getBasicBlocks()) {
      if(bb != cfg.getEntry() && bb != cfg.getExit()) {
        for(Statement stmt : bb.getStatements()) {
          if(stmt instanceof Assignment) {
            LValue var = ((Assignment) stmt).getLHS();
            if(!registers.containsKey(var)) {
              registers.put(var, nextSlot);
              int numSlots = numSlots(var);
              nextSlot += numSlots;
              size += numSlots;
            }
          }
        }
      }
    }
  }

  public int getRegister(LValue var) {
    Integer register = registers.get(var);
    Preconditions.checkNotNull(register, "no register assigned for " + var);
    return register;
  }

  private int numSlots(LValue var) {
    throw new UnsupportedOperationException("TODO");
  }

  public int getSize() {
    return size;
  }

  @Override
  public String toString() {
    return "RegisterAllocation: "  + registers.toString();
  }
}
