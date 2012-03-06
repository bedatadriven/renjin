package r.compiler;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import r.lang.SEXP;

import com.google.common.collect.Lists;

/**
 * Maintains a pool of SEXP literals stored in class fields.
 *
 */
public class SexpPool implements Opcodes {

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
  
  List<Entry> entries = Lists.newArrayList();
  
  public String add(SEXP sexp, String type) {
    Entry entry = new Entry(sexp, type, "sexp" + entries.size());
    entries.add(entry);
    return entry.getFieldName();
  }
  
  public List<Entry> entries() {
    return entries;
  }

  public void writeFields(ClassVisitor cv) {
    for(SexpPool.Entry entry : entries()) {
      cv.visitField(ACC_PRIVATE, entry.getFieldName(), 
          entry.getType(), null, null);
    }    
  }
}
