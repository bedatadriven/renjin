package org.renjin.gcc.translate.expr;

import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;

/**
 * An intermediate expression that references a memory location
 * (and is backed by a JVM array)
 */
public interface ImIndirectExpr extends ImExpr {

  ArrayRef translateToArrayRef(FunctionContext context);

  @Override
  ImIndirectType type();
}
