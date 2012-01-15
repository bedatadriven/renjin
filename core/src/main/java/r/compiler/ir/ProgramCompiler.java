package r.compiler.ir;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;


import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.ir.tac.IRProgram;
import r.compiler.ir.tac.IRScope;
import r.compiler.ir.tree.TreeBuilder;
import r.compiler.ir.tree.TreeNode;
import r.lang.SEXP;

public class ProgramCompiler implements Opcodes {

  private IRProgram program;

  public void compile(SEXP exp) {
    
    program = new IRProgram(exp);
    
    compileMain();
  }
  
  private void compileMain() {

    ClassWriter cw = new ClassWriter(0);
    FieldVisitor fv;
    MethodVisitor mv;
    AnnotationVisitor av0;
    
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "RProgram", null, "java/lang/Object", new String[] { });
    
    writeConstructor(cw);
    writeMainMethod(cw);
    
  }
  

  private void writeConstructor(ClassWriter cw) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(10, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lr/benchmarks/MeanOnline;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  

  private void writeMainMethod(ClassWriter cw) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    mv.visitCode();
    mv.visitMethodInsn(INVOKESTATIC, "r/lang/Context", "newTopLevelContext", "()Lr/lang/Context;");
    mv.visitVarInsn(ASTORE, 1);

    
    
    mv.visitEnd();
    
  }
  
  private void compileBody(IRScope scope) {
    
    ControlFlowGraph cfg = new ControlFlowGraph(scope);
    TreeBuilder builder = new TreeBuilder();
    
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      List<TreeNode> nodes = builder.build(bb);
            
      for(TreeNode node : nodes) {
        System.out.println(node);
      }
    }
     
  }
  
}
