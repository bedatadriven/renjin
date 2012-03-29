package org.renjin.compiler;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;


import com.google.common.collect.Lists;

/**
 * Maintains a pool of SEXP literals stored in class fields.
 *
 */
public class FieldSexpPool implements Opcodes, SexpPool {

  public static class Entry {
    private SEXP sexp;
    private String type;
    private String fieldName;
    
    public Entry(SEXP sexp, String className, String fieldName) {
      super();
      this.sexp = sexp;
      this.type = className;
      this.fieldName = fieldName;
    } 
    
    public SEXP getSexp() {
      return sexp;
    }
    
    public String getFieldName() {
      return fieldName;
    }

    public String getType() {
      return type;
    }
  }
  
  protected String className;
  protected List<Entry> entries = Lists.newArrayList();
  
  
  public FieldSexpPool(String className) {
    super();
    this.className = className;
  }

  protected String add(SEXP sexp, String type) {
    Entry entry = new Entry(sexp, type, "sexp" + entries.size());
    entries.add(entry);
    return entry.getFieldName();
  }
  
  public List<Entry> entries() {
    return entries;
  }

  public void writeFields(ClassVisitor cv) {
    for(FieldSexpPool.Entry entry : entries()) {
      cv.visitField(ACC_PRIVATE, entry.getFieldName(), 
          entry.getType(), null, null);
    }    
  }

  @Override
  public void writeConstructorBody(MethodVisitor mv) {

    ConstantGeneratingVisitor cgv = new ConstantGeneratingVisitor(mv);
    for(FieldSexpPool.Entry entry : entries) {
      mv.visitVarInsn(ALOAD, 0); // this
      entry.getSexp().accept(cgv);
      mv.visitFieldInsn(PUTFIELD, 
          className, entry.getFieldName(), entry.getType());
    }
        
  }

  @Override
  public void pushSexp(MethodVisitor mv, FunctionCall call, String string) {
    mv.visitVarInsn(ALOAD, 0); // this
    mv.visitFieldInsn(GETFIELD, className,
          add(call, "Lorg/renjin/sexp/FunctionCall;"), "Lorg/renjin/sexp/FunctionCall;");
  }

  @Override
  public void writeStaticInitializerBody(MethodVisitor mv) {
      // noop
  }
}
