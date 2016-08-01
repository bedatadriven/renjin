package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;

/**
 * Common interface to generating code for local and global variables.
 *
 * @see LocalVarAllocator
 * @see GlobalVarAllocator
 */
public abstract class VarAllocator {


  public abstract JLValue reserve(String name, Type type);
  
  public abstract JLValue reserve(String name, Type type, JExpr initialValue);

  public final JLValue reserve(String name, Class type) {
    return reserve(name, Type.getType(type));
  }

  public final JLValue reserveArrayRef(String name, Type componentType) {
    return reserve(name, Type.getType("[" + componentType.getDescriptor()));
  }
  
  public final JLValue reserveUnitArray(String name, Type componentType, Optional<JExpr> initialValue) {

    JExpr newArray;
    if(initialValue.isPresent()) {
      newArray = Expressions.newArray(componentType, Collections.singletonList(initialValue.get()));
    } else {
      newArray = Expressions.newArray(componentType, 1);
    }
    return reserve(name, Type.getType("[" + componentType.getDescriptor()), newArray);
  }

  public final JLValue reserveInt(String name) {
    return reserve(name, Type.INT_TYPE);
  }
  
  public final JLValue reserveOffsetInt(String name) {
    if(name == null) {
      return reserve(null, Type.INT_TYPE);
    } else {
      return reserve(name + "$offset", Type.INT_TYPE);
    }
  }
  

  public static String toJavaSafeName(String name) {
    return name.replace('.', '$');
  }

}
