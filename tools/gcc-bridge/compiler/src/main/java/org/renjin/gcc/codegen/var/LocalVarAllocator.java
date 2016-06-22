package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator extends VarAllocator {



  public static class LocalVar implements JLValue {
    private String name;
    private int index;
    private Type type;
    private Optional<JExpr> initialValue;

    public LocalVar(String name, int index, Type type, Optional<JExpr> value) {
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
    public void store(MethodGenerator mv, JExpr value) {
      value.load(mv);
      store(mv);
    }
    
    public void store(MethodGenerator mv) {
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
    return reserve(name, type, Optional.<JExpr>absent());
  }

  @Override
  public LocalVar reserve(String name, Type type, JExpr initialValue) {
    return reserve(name, type, Optional.of(initialValue));
  }


  public LocalVar reserve(Type type) {
    return reserve(null, type);
  }
  
  private LocalVar reserve(String name, Type type, Optional<JExpr> initialValue) {
    int index = slots;
    slots += type.getSize();
    LocalVar var = new LocalVar(name, index, type, initialValue);
    names.add(var);
    return var;
  }
  
  public void initializeVariables(MethodGenerator mv) {
    List<LocalVar> toInitialize = Lists.newArrayList(names);
    for (LocalVar name : toInitialize) {
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
