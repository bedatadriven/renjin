package org.renjin.gcc.codegen;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Map;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator {
  
  
  private static class LocalVar implements Var {
    private int index;
    private Type type;

    public LocalVar(int index, Type type) {
      this.index = index;
      this.type = type;
    }

    @Override
    public void load(MethodVisitor mv) {
      mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
    }

    @Override
    public void store(MethodVisitor mv) {
      mv.visitVarInsn(type.getOpcode(Opcodes.ISTORE), index);
    }
  }
  
  private int slots = 0;
  private Map<String, LocalVar> names = Maps.newHashMap();

  public Var reserve(String name, Type type) {
    Preconditions.checkState(!names.containsKey(name), "Variable name already used: " + name);
    int index = slots;
    slots += type.getSize();
    LocalVar var = new LocalVar(index, type);
    names.put(name, var);
    return var;
  }
  
  public final Var reserve(String name, Class type) {
    return reserve(name, Type.getType(type));
  }

  public final Var reserveArrayRef(String name, Type componentType) {
    return reserve(name, Type.getType("[" + componentType.getDescriptor()));
  }

  public final Var reserveInt(String name) {
    return reserve(name, Type.INT_TYPE);
  }

  public void emitDebugging(MethodVisitor mv, Label start, Label end) {

    for (Map.Entry<String, LocalVar> entry : names.entrySet()) {
      String name = entry.getKey();
      LocalVar desc = entry.getValue();

      mv.visitLocalVariable(name, desc.type.getDescriptor(), null, start, end, desc.index);
    }
  }
}
