package org.renjin.primitives.packaging;

import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;

/**
 * A symbol registered with a dynamic library
 */
public class DllSymbol {

  public enum Convention {
    C("CRoutine"),
    CALL("CallRoutine"),
    FORTRAN("FortranRoutine"),
    EXTERNAL("ExternalRoutine");
    
    private String className;

    Convention(String className) {
      this.className = className;
    }

    public String getClassName() {
      return className;
    }
  }

  private String name;
  private DllInfo library;
  private MethodHandle methodHandle;
  private Convention convention;

  public DllSymbol(DllInfo library) {
    this.library = library;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DllInfo getLibrary() {
    return library;
  }

  public void setMethodHandle(MethodHandle methodHandle) {
    this.methodHandle = methodHandle;
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

  public void setLibrary(DllInfo library) {
    this.library = library;
  }

  public void setConvention(Convention convention) {
    this.convention = convention;
  }
  

  /**
   * @return an R NativeSymbolInfo SEXP object
   */
  public ListVector createObject() {

    ListVector.NamedBuilder symbol = new ListVector.NamedBuilder();
    symbol.add("name", name);
    symbol.add("address", new ExternalPtr<MethodHandle>(methodHandle,
        AttributeMap.builder().setClass("RegisteredNativeSymbol").build()));

    symbol.add("numParameters", methodHandle.type().parameterCount());
    
    symbol.setAttribute(Symbols.CLASS_NAME, new StringArrayVector(convention.getClassName(), "NativeSymbolInfo"));
    
    return symbol.build();
  }
}
