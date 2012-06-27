package org.renjin.compiler.ir;

import org.renjin.compiler.CompiledBody;
import org.renjin.eval.Context;
import org.renjin.sexp.*;


public class TestCompiled implements CompiledBody {

  @Override
  public SEXP eval(Context context, Environment rho) {
    
    Vector x = new DoubleArrayVector(1);
    int i = x.getElementAsInt(4);
    
    return Null.INSTANCE;
   
  }

}
