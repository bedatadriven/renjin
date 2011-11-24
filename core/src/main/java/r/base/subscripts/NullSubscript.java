package r.base.subscripts;

public class NullSubscript extends Subscript {

  public static final NullSubscript INSTANCE = new NullSubscript();
  
  private NullSubscript() { }
  
  @Override
  public int getCount() {
     return 0;
  }  
}
