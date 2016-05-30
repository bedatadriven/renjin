package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.repackaged.guava.base.Optional;

/**
 * Common interface to generating code for local and global variables.
 *
 * @see LocalVarAllocator
 * @see GlobalVarAllocator
 */
public abstract class VarAllocator {


  public abstract SimpleLValue reserve(String name, Type type);
  
  public abstract SimpleLValue reserve(String name, Type type, SimpleExpr initialValue);

  public final SimpleLValue reserve(String name, Class type) {
    return reserve(name, Type.getType(type));
  }

  public final SimpleLValue reserveArrayRef(String name, Type componentType) {
    return reserve(name, Type.getType("[" + componentType.getDescriptor()));
  }
  
  public final SimpleLValue reserveUnitArray(String name, Type componentType, Optional<SimpleExpr> initialValue) {

    SimpleExpr newArray;
    if(initialValue.isPresent()) {
      newArray = Expressions.newArray(initialValue.get());
    } else {
      newArray = Expressions.newArray(componentType, 1);
    }
    return reserve(name, Type.getType("[" + componentType.getDescriptor()), newArray);
  }

  public final SimpleLValue reserveInt(String name) {
    return reserve(name, Type.INT_TYPE);
  }


  public static String toJavaSafeName(String name) {
    return name.replace('.', '$');
  }

}
