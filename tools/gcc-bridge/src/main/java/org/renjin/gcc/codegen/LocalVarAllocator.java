package org.renjin.gcc.codegen;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Map;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator {
  
  
  private static class VarDescriptor {
    private int index;
    private Type type;

    public VarDescriptor(int index, Type type) {
      this.index = index;
      this.type = type;
    }
  }
  
  private int slots = 0;
  private Map<String, VarDescriptor> names = Maps.newHashMap();
  
  public int reserve(int numSlots) {
    int index = slots;
    slots += numSlots;
    return index;
  }
  
  public int reserve(String name, Type type) {
    Preconditions.checkState(!names.containsKey(name), "Variable name already used: " + name);
    int index = reserve(type.getSize());
    names.put(name, new VarDescriptor(index, type));
    return index;
  }
  
  public final int reserve(String name, Class type) {
    return reserve(name, Type.getType(type));
  }

  public final int reserveArrayRef(String name, Type componentType) {
    return reserve(name, Type.getType("[" + componentType.getDescriptor()));
  }

  public final int reserveInt(String name) {
    return reserve(name, Type.INT_TYPE);
  }
  
  public int size() {
    return slots;
  }
  
  public void emitDebugging(MethodVisitor mv, Label start, Label end) {

    for (Map.Entry<String, VarDescriptor> entry : names.entrySet()) {
      String name = entry.getKey();
      VarDescriptor desc = entry.getValue();

      mv.visitLocalVariable(name, desc.type.getDescriptor(), null, start, end, desc.index);
    }
  }
}
