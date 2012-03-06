package r.compiler;

public class GenerationContext {
  private String className;
  private SexpPool sexpPool;
  private ThunkMap thunkMap;
  
  public GenerationContext(String className, ThunkMap thunkMap) {
    this.className = className;
    this.sexpPool = new SexpPool();
    this.thunkMap = thunkMap;
  }
  
  public String getClassName() {
    return className;
  }

  public SexpPool getSexpPool() {
    return sexpPool;
  }
}
