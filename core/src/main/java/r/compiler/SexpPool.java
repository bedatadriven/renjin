package r.compiler;

import java.util.List;

import r.lang.SEXP;

import com.google.common.collect.Lists;

public class SexpPool {

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
  
}
