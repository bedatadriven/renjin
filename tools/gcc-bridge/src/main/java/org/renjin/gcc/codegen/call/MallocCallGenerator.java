package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Created by alex on 1-12-15.
 */
public class MallocCallGenerator implements CallGenerator {
  
  private GeneratorFactory generators;

  public MallocCallGenerator(GeneratorFactory generators) {
    this.generators = generators;
  }

  @Override
  public void emitCall(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
    
  }

  @Override
  public void emitCallAndPopResult(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
      // NOOP
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    ExprGenerator size = argumentGenerators.get(0);
    return generators.forType(returnType).mallocExpression(size);
  }
}
