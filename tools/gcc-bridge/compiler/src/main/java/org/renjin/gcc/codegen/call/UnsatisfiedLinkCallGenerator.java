package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.runtime.UnsatisfiedLinkException;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;

import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * Throws a runtime exception.
 */
public class UnsatisfiedLinkCallGenerator implements CallGenerator, MethodHandleGenerator {

  public static final Type HANDLE_TYPE = Type.getType(MethodHandle.class);
  private String functionName;

  public UnsatisfiedLinkCallGenerator(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    Type exceptionType = Type.getType(UnsatisfiedLinkException.class);
    mv.anew(exceptionType);
    mv.dup();
    mv.aconst(functionName);
    mv.invokeconstructor(exceptionType, Type.getType(String.class));
    mv.athrow();
  }

  @Override
  public SimpleExpr getMethodHandle() {

    // Create a method handle that throws the UnsatisifiedLinkException.

    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return HANDLE_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.aconst(functionName);
        mv.invokestatic(UnsatisfiedLinkException.class, "throwingHandle", 
            getMethodDescriptor(HANDLE_TYPE, Type.getType(String.class)));
      }
    };
  }
}
