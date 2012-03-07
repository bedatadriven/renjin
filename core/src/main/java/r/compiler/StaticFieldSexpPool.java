package r.compiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import r.lang.FunctionCall;

public class StaticFieldSexpPool extends FieldSexpPool {

  public StaticFieldSexpPool(String className) {
    super(className);
  }
  
  public void writeFields(ClassVisitor cv) {
    for(FieldSexpPool.Entry entry : entries()) {
      cv.visitField(ACC_PRIVATE + ACC_STATIC, entry.getFieldName(), 
          entry.getType(), null, null);
    }    
  }

  @Override
  public void writeConstructorBody(MethodVisitor mv) {
      
  }

  @Override
  public void pushSexp(MethodVisitor mv, FunctionCall call, String type) {
    mv.visitFieldInsn(GETSTATIC, className, add(call, type), type);
  }

}
