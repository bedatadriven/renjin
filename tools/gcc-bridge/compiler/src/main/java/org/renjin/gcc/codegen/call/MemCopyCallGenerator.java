package org.renjin.gcc.codegen.call;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.voidt.VoidPtrStrategy;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;


/**
 * Generates calls to memcpy() depending on the type of its arguments
 */
public class MemCopyCallGenerator implements CallGenerator {
  
  public static final String BUILTIN_MEMCPY = "__builtin_memcpy";
  public static final String MEMMOVE = "memmove";

  private final TypeOracle typeOracle;
  private final boolean buffer;

  public MemCopyCallGenerator(TypeOracle typeOracle, boolean buffer) {
    this.typeOracle = typeOracle;
    this.buffer = buffer;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    
    if(call.getOperands().size() != 3) {
      throw new InternalCompilerException("memcpy() expects 3 args.");
    }

    GimpleExpr destination = call.getOperand(0);
    GimpleExpr source = call.getOperand(1);

    GExpr sourcePtr =  exprFactory.findGenerator(source);
    GExpr destinationPtr = exprFactory.findGenerator(destination);
    JExpr length = exprFactory.findPrimitiveGenerator(call.getOperand(2));

    PointerTypeStrategy sourceStrategy = typeOracle.forPointerType(source.getType());
    PointerTypeStrategy destinationStrategy = typeOracle.forPointerType(destination.getType());

    try {
      if (sourceStrategy instanceof VoidPtrStrategy) {
        sourcePtr = destinationStrategy.cast(mv, sourcePtr, sourceStrategy);
        sourceStrategy = destinationStrategy;
      } else {
        destinationPtr = sourceStrategy.cast(mv, destinationPtr, destinationStrategy);
        destinationStrategy = sourceStrategy;
      } 
    } catch (UnsupportedCastException e) {
      throw new InternalCompilerException(String.format("memcpy(%s, %s): incompatible pointer types",
          destinationStrategy, 
          sourceStrategy));
    }
    
    sourceStrategy.memoryCopy(mv, destinationPtr, sourcePtr, length, buffer);
 
    if(call.getLhs() != null) {
      // memcpy() returns the destination pointer
      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      PointerTypeStrategy lhsStrategy = typeOracle.forPointerType(call.getLhs().getType());

      try {
        lhs.store(mv, lhsStrategy.cast(mv, destinationPtr, destinationStrategy));
      } catch (UnsupportedCastException e) {
        throw new InternalCompilerException(String.format("Cannot assign result of memcpy => %s to %s\n", 
            destinationStrategy, lhsStrategy));
      }
    }
  }
}
