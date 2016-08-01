package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;

/**
 * Layout for a record that has no fields.
 */
public class EmptyRecordLayout implements RecordLayout {
  @Override
  public Type getType() {
    return Type.getType(Object.class);
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    // NOOP
  }

  @Override
  public void writeClassFiles(File outputDir) throws IOException {
    // NOOP
  }

  @Override
  public GExpr memberOf(RecordValue instance, GimpleFieldRef fieldRef, TypeStrategy fieldTypeStrategy) {
    throw new UnsupportedOperationException("Empty record has no fields.");
  }
}
