package r.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.lang.DoubleVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.SexpVisitor;

public class ConstantGeneratingVisitor extends SexpVisitor<Void> implements Opcodes {
  private MethodVisitor mv;

  public ConstantGeneratingVisitor(MethodVisitor mv) {
    super();
    this.mv = mv;
  }

  @Override
  public void visit(DoubleVector vector) {
    
    if(vector.length() == 1) {
      mv.visitTypeInsn(NEW, "r/lang/DoubleVector");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_1);
      mv.visitIntInsn(NEWARRAY, T_DOUBLE);
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      mv.visitLdcInsn(vector.getElementAsDouble(0));
      mv.visitInsn(DASTORE);
      mv.visitMethodInsn(INVOKESPECIAL, "r/lang/DoubleVector", "<init>", "([D)V");
    } else {
      throw new UnsupportedOperationException("only double vectors of length 1 are implemented");
    }
  }
    
  @Override
  public void visit(Null nullExpression) {
    mv.visitFieldInsn(GETSTATIC, "r/lang/Null", "INSTANCE", "Lr/lang/Null;");
  }

  @Override
  public void visit(LogicalVector vector) {
    if(vector.length() == 1) {
      if(vector.getElementAsRawLogical(0) == 1) {
        mv.visitFieldInsn(GETSTATIC, "r/lang/LogicalVector", "TRUE", "Lr/lang/LogicalVector;");
      } else if(vector.getElementAsRawLogical(0) == 0) {
        mv.visitFieldInsn(GETSTATIC, "r/lang/LogicalVector", "FALSE", "Lr/lang/LogicalVector;");
      } else {
        throw new UnsupportedOperationException("nyi");
      }
    } else {
      throw new UnsupportedOperationException("nyi");
    }
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new UnsupportedOperationException("Constant generation nyi for " + exp);
  }
  
  
}
