package org.renjin.compiler;

import java.io.PrintWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.expressions.IRThunk;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.sexp.Closure;


/**
 * Compiles a Thunk to a subclass of a Promise sexp 
 */
public class ThunkCompiler implements Opcodes {

  private IRThunk thunk;
  private ClassWriter cw;
  private ClassVisitor cv;
  private GenerationContext generationContext;

  
  public static byte[] compile(String className, ThunkMap thunkMap, IRThunk thunk) {
    return new ThunkCompiler(thunkMap, className)
    .doCompile(thunk);    
  }
  
  public ThunkCompiler(ThunkMap thunkMap, String className) {
    super();
    
    this.generationContext = new GenerationContext(className,
        new StaticFieldSexpPool(className),
        thunkMap);
  }

  public byte[] doCompile(IRThunk thunk) {
    this.thunk = thunk;
    startClass();
    writeStaticDoEval();
    writeConstructor();
    writeSexp();
    generationContext.getSexpPool().writeFields(cv);
    writeClassEnd();
    return cw.toByteArray();
  }

  private void startClass() {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    //cw = new ClassWriter(0);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
  //  cv = new CheckClassAdapter(cv);
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, generationContext.getClassName(), null, "org/renjin/sexp/Promise", null);

  }

  private void writeConstructor() {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(INVOKESTATIC, generationContext.getClassName(), "createSexp", "()Lorg/renjin/sexp/SEXP;");
    mv.visitMethodInsn(INVOKESPECIAL, "org/renjin/sexp/Promise", "<init>", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;Lorg/renjin/sexp/SEXP;)V");
      
    // initialize sexp pool
    generationContext.getSexpPool().writeConstructorBody(mv);
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeStaticDoEval() {  
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, "doEval", "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/Environment;)Lorg/renjin/sexp/SEXP;", null, null);
    mv.visitCode();
    writeDoEvalBody(mv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  
  private void writeSexp() {
    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, 
        "createSexp", "()Lorg/renjin/sexp;", null, null);
    mv.visitCode();
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    thunk.getSExpression().accept(cgv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
 }
 
  private void writeDoEvalBody(MethodVisitor mv) {
    IRBody body = thunk.getBody();

    generationContext.setContextLdc(0);
    generationContext.setEnvironmentLdc(1);
    ByteCodeVisitor visitor = new ByteCodeVisitor(generationContext, mv);
    
    
    ControlFlowGraph cfg = new ControlFlowGraph(body);
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      
      System.out.println(bb.statementsToString());
      
      visitor.startBasicBlock(bb);
      
    //  List<Statement> statements = TreeBuilder.build(bb);
      for(Statement stmt : bb.getStatements()) {
        stmt.accept(visitor);
      }
    }
    
  }

  private void writeClassEnd() {
    cv.visitEnd();
  }
}
