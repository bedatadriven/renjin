package r.compiler.ir;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import r.lang.DoubleVector;
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
  
  
}
