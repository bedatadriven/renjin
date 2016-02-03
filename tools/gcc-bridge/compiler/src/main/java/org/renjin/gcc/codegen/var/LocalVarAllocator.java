package org.renjin.gcc.codegen.var;

import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;

import java.util.List;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator extends VarAllocator {
  
  
  private static class LocalVar implements Var {
    private String name;
    private int index;
    private Type type;

    public LocalVar(String name, int index, Type type) {
      this.name = name;
      this.index = index;
      this.type = type;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public void load(MethodGenerator mv) {
      mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
    }

    @Override
    public void store(MethodGenerator mv, Value value) {
      value.load(mv);
      mv.visitVarInsn(type.getOpcode(Opcodes.ISTORE), index);
    }
  }
  
  
  
  private int slots = 0;
  private List<LocalVar> names = Lists.newArrayList();

  @Override
  public Var reserve(String name, Type type) {
    int index = slots;
    slots += type.getSize();
    LocalVar var = new LocalVar(name, index, type);
    names.add(var);
    return var;
  }

  public void emitDebugging(MethodGenerator mv, Label start, Label end) {

    for (LocalVar entry : names) {

      mv.visitLocalVariable(toJavaSafeName(entry.name), entry.type.getDescriptor(), null, start, end, entry.index);
    }
  }


  private String toJavaSafeName(String name) {
    return name.replace('.', '$');
  }
}
