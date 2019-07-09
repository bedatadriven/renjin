package org.renjin.compiler.aot;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.ValueBoundsMap;
import org.renjin.compiler.ir.tac.functions.TranslationContext;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.FunctionEnvironment;
import org.renjin.sexp.Symbol;

import java.util.List;

public class ClosureTranslationContext implements TranslationContext {

  private static final ValueBounds MISSING_BOUNDS = ValueBounds.builder()
      .setTypeSet(TypeSet.LOGICAL)
      .setLength(1)
      .addFlags(ValueBounds.FLAG_NO_NA)
      .build();

  @Override
  public boolean isEllipsesArgumentKnown() {
    return false;
  }

  @Override
  public List<IRArgument> getEllipsesArguments() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Expression isMissing(Symbol name) {
    return new Expression() {
      @Override
      public boolean isPure() {
        return true;
      }

      @Override
      public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
        return MISSING_BOUNDS;
      }

      @Override
      public ValueBounds getValueBounds() {
        return MISSING_BOUNDS;
      }

      @Override
      public CompiledSexp getCompiledExpr(EmitContext emitContext) {

        int frameIndex;
        if(emitContext instanceof ClosureEmitContext) {
          frameIndex = ((ClosureEmitContext) emitContext).getFrameVarIndex(name);
        } else {
          frameIndex = -1;
        }

        return new ScalarExpr(VectorType.LOGICAL) {
          @Override
          public void loadScalar(EmitContext context, InstructionAdapter mv) {


            mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
            mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());

            if(frameIndex == -1) {
              mv.aconst(name.getPrintName());
              mv.invokevirtual(Type.getInternalName(FunctionEnvironment.class), "isMissingArgument",
                  Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                      Type.getType(Context.class),
                      Type.getType(String.class)), false);
            } else {
              mv.iconst(frameIndex);
              mv.invokevirtual(Type.getInternalName(FunctionEnvironment.class), "isMissingArgument",
                  Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                      Type.getType(Context.class),
                      Type.getType(String.class)), false);
            }
          }
        };
      }

      @Override
      public void setChild(int childIndex, Expression child) {
        throw new UnsupportedOperationException("TODO");
      }

      @Override
      public int getChildCount() {
        throw new UnsupportedOperationException("TODO");
      }

      @Override
      public Expression childAt(int index) {
        throw new UnsupportedOperationException("TODO");
      }
    };
  }
}
