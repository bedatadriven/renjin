package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;


public interface RecordLayout {
  
  Type getType();

  void linkFields(TypeOracle typeOracle);
  
  void writeClassFiles(File outputDir) throws IOException;
  
  GExpr memberOf(MethodGenerator mv, RecordValue instance, GimpleFieldRef fieldRef, TypeStrategy fieldTypeStrategy);
  
}
