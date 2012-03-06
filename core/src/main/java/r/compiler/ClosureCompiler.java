package r.compiler;

import java.io.PrintWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.ir.tac.IRBody;
import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.IRFunctionTable;
import r.compiler.ir.tac.statements.Statement;
import r.lang.Closure;

public class ClosureCompiler implements Opcodes {

  private Closure closure;
  private ClassWriter cw;
  private ClassVisitor cv;
  private GenerationContext generationContext;

  public static Class<Closure> compileAndLoad(Closure closure) {
    return new ClosureCompiler("Closure" + System.identityHashCode(closure))
      .doCompileAndLoad(closure);
  }
  
  public static byte[] compile(String className, Closure closure) {
    return new ClosureCompiler(className)
    .doCompile(closure);    
  }
  
  public ClosureCompiler(String className) {
    super();
    
    this.generationContext = new GenerationContext(className,
        new ThunkMap(className + "$"));
  }

  public byte[] doCompile(Closure closure) {
    this.closure = closure;
    startClass();
    writeDoEval();
    writeConstructor();
    writeFormals();
    writeBodySexp();
    generationContext.getSexpPool().writeFields(cv);
    writeClassEnd();
    return cw.toByteArray();
  }

  public Class<Closure> doCompileAndLoad(Closure closure) {
    return new MyClassLoader().defineClass(generationContext.getClassName().replace('/', '.'), doCompile(closure)); 
  }
  
  private void startClass() {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    //cw = new ClassWriter(0);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
  //  cv = new CheckClassAdapter(cv);
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, generationContext.getClassName(), null, "r/lang/Closure", null);

  }

  private void writeConstructor() {

    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "(Lr/lang/Environment;)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, generationContext.getClassName(), "createFormals", "()Lr/lang/PairList;");
    mv.visitMethodInsn(INVOKESTATIC, generationContext.getClassName(), "createBody", "()Lr/lang/SEXP;");
    mv.visitMethodInsn(INVOKESPECIAL, "r/lang/Closure", "<init>", "(Lr/lang/Environment;Lr/lang/PairList;Lr/lang/SEXP;)V");
      
    // initialize sexp pool
  
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    for(SexpPool.Entry entry : generationContext.getSexpPool().entries()) {
      mv.visitInsn(DUP); // keep "this" on the stack
      entry.getSexp().accept(cgv);
      mv.visitFieldInsn(PUTFIELD, 
          generationContext.getClassName(), entry.getFieldName(), entry.getType());
    }
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void writeDoEval() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "doApply", 
        "(Lr/lang/Context;)Lr/lang/SEXP;", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "r/lang/Context", "getEnvironment", "()Lr/lang/Environment;");
    mv.visitVarInsn(ASTORE, 2);

    writeDoEvalBody(mv);
    
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  private void writeFormals() {
  
    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, 
        "createFormals", "()Lr/lang/PairList;", null, null);
    mv.visitCode();
    
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    closure.getFormals().accept(cgv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
  }

  private void writeBodySexp() {

    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, 
        "createBody", "()Lr/lang/SEXP;", null, null);
    mv.visitCode();
    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    closure.getBody().accept(cgv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 0);
    mv.visitEnd();
 }
 
  private void writeDoEvalBody(MethodVisitor mv) {
    IRFunctionTable functionTable = new IRFunctionTable();
    IRBodyBuilder builder = new IRBodyBuilder(functionTable);
    IRBody body = builder.build(closure.getBody());
    
    System.out.println(body);
    
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

  class MyClassLoader extends ClassLoader {
    public Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
   }
  }
}
