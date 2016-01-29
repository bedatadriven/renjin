package org.renjin.gcc.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.gcc.codegen.var.LocalVarAllocator;


public class MethodGenerator extends InstructionAdapter {
  
  private final LocalVarAllocator localVarAllocator = new LocalVarAllocator();
  
  public MethodGenerator(MethodVisitor mv) {
    super(Opcodes.ASM5, mv);
  }

  public LocalVarAllocator getLocalVarAllocator() {
    return localVarAllocator;
  }
}
