package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Lists;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator extends VarAllocator {


  public static class LocalVar implements SimpleLValue {
    private String name;
    private int index;
    private Type type;
    private Optional<SimpleExpr> initialValue;

    public LocalVar(String name, int index, Type type, Optional<SimpleExpr> value) {
      this.name = name;
      this.index = index;
      this.type = type;
      this.initialValue = value;
    }

    @Nonnull
    @Override
    public Type getType() {
      return type;
    }
    
    @Override
    public void load(@Nonnull MethodGenerator mv) {
      mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
    }

    public int getIndex() {
      return index;
    }

    @Override
    public void store(MethodGenerator mv, SimpleExpr value) {
      value.load(mv);
      mv.visitVarInsn(type.getOpcode(Opcodes.ISTORE), index);
    }

    @Override
    public String toString() {
      return "LocalVar[" + name + ":" + type + "]";
    }
  }

  private int slots = 0;
  private List<LocalVar> names = Lists.newArrayList();

  @Override
  public LocalVar reserve(String name, Type type) {
    return reserve(name, type, Optional.<SimpleExpr>absent());
  }

  @Override
  public LocalVar reserve(String name, Type type, SimpleExpr initialValue) {
    return reserve(name, type, Optional.of(initialValue));
  }
  
  private LocalVar reserve(String name, Type type, Optional<SimpleExpr> initialValue) {
    int index = slots;
    slots += type.getSize();
    LocalVar var = new LocalVar(name, index, type, initialValue);
    names.add(var);
    return var;
  }
  
  public void initializeVariables(MethodGenerator mv) {
    for (LocalVar name : names) {
      if(name.initialValue.isPresent()) {
        name.store(mv, name.initialValue.get());
      }
    }
  }

  public void emitDebugging(MethodGenerator mv, Label start, Label end) {

    for (LocalVar entry : names) {
      if(entry.name != null) {
        mv.visitLocalVariable(toJavaSafeName(entry.name), entry.type.getDescriptor(), null, start, end, entry.index);
      }
    }
  }


}
